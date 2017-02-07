package com.pcdn.pool

import java.util.Random

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import org.apache.thrift.transport._

import scala.concurrent.duration._
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.language.postfixOps


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
        if (deadServers.nonEmpty) deadServers.foreach {
          s => if (check(s)) activeServer(s)
        }
      case "givemeconnection" => {
        val s: ActorRef = sender()
        Future { get (activeServers.toList, s) }
      }
    }

    private def get(servers: List[Server], sender: ActorRef): Unit = {
      if (servers.isEmpty) None
      else {
        val num = if (servers.length == 1) 0 else new Random().nextInt(servers.length)
        val server = servers(num)
        createConnection(server) match {
          case None =>
            deactivateServer(server)
            get(activeServers.toList, sender)
          case x => sender ! x
        }
      }
    }

    private def check(server: Server): Boolean = {
      createConnection(server) match {
        case None => false
        case _ => true
      }
    }

    private def createConnection(server: Server): Option[TTransport] = {
      val tTransportFactory = new TTransportFactory()
      val tFramedTransport: TFramedTransport = new TFramedTransport(new TSocket(server.host,
        server.port, 200))
      val transport: TTransport = tTransportFactory.getTransport(tFramedTransport)
      try {
        transport.open()
        Some(transport)
      } catch {
        case ex: TTransportException => None
      }
    }
  }
}
