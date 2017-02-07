package com.pcdn.pool

import java.util.Random

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import org.apache.thrift.transport._

import scala.concurrent.duration._
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.language.postfixOps
import scala.util.{Failure, Success}


/**
  * Created by Hung on 1/18/17.
  */

object BackendOperator {

  private val activeServers = ListBuffer[Server]()
  private val deadServers = ListBuffer[Server]()

  import scala.concurrent.ExecutionContext.Implicits.global

  private final val system = ActorSystem()

  def apply(servers: Servers): ActorRef = {
    servers.info.foreach(s => activeServers += s)
    val actor = system.actorOf(Props[BackendOperator])
    system.scheduler.schedule(5 seconds, 5 seconds, actor, "check")
    actor
  }

  private def deactivateServer(server: Server) = {
    deadServers += server
    activeServers -= server
  }


  private def activeServer(server: Server) = {
    activeServers += server
    deadServers -= server
  }


  class BackendOperator extends Actor {
    override def receive: Receive = {
      case "check" =>
        if (deadServers.nonEmpty) deadServers.foreach(check)
      case "givemeconnection" =>
        val s: ActorRef = sender()
        Future {
          get(activeServers.toList, s)
        }
    }

    private def get(servers: List[Server], sender: ActorRef): Unit = {
      if (servers.isEmpty) throw PoolConnectionException("No available server")
      else {
        val num = if (servers.length == 1) 0 else new Random().nextInt(servers.length)
        val server = servers(num)
        createConnection(server) onComplete {
          case Failure(_) =>
            deactivateServer(server)
            get(activeServers.toList, sender)
          case Success(x) => sender ! x
        }
      }
    }

    private def check(server: Server) = {
      createConnection(server) onComplete {
        case Failure(_) => ()
        case Success(_) => activeServer(server)
      }
    }


    private def createConnection(server: Server): Future[TTransport] = Future {
      val tTransportFactory = new TTransportFactory()
      val tFramedTransport: TFramedTransport = new TFramedTransport(new TSocket(server.host,
        server.port, 200))
      val transport: TTransport = tTransportFactory.getTransport(tFramedTransport)
      transport.open()
      transport
    }
  }

}
