package com.pcdn.pool

import akka.pattern.ask
import akka.util.Timeout
import org.apache.commons.pool2.impl.DefaultPooledObject
import org.apache.commons.pool2.{PooledObject, PooledObjectFactory}
import org.apache.thrift.TServiceClient
import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.transport.TTransport

import scala.language.postfixOps
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor}
import scala.util.{Failure, Success, Try}

/**
  * Created by Hung on 1/12/17.
  *
  *
  */
case class PoolConnectionException(message: String = "", cause: Throwable = null)
  extends Exception(message, cause)

object GenericThriftObjectPool {
  implicit val timeout = Timeout(200 milliseconds)
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  def apply[T <: TServiceClient : Manifest](servers: Servers): GenericThriftObjectPool[T] = {
    new GenericThriftObjectPool[T](servers)
  }

  private[pool] class GenericThriftObjectPool[T <: TServiceClient : Manifest](val servers: Servers)
    extends PooledObjectFactory[T] {

    val backendOperator = BackendOperator(servers)

    override def destroyObject(p: PooledObject[T]): Unit = {
      val obj: T = p.getObject
      obj.getInputProtocol.getTransport.close()
    }

    override def validateObject(p: PooledObject[T]): Boolean = {
      val obj: T = p.getObject
      obj.getInputProtocol.getTransport.isOpen
    }

    override def activateObject(p: PooledObject[T]): Unit = {
      val obj: T = p.getObject
      if (!obj.getInputProtocol.getTransport.isOpen) {
        obj.getInputProtocol.getTransport.open()
      }
    }

    override def passivateObject(p: PooledObject[T]): Unit = destroyObject(p)


    private def borrowConn(): Try[TTransport] = {
      Try {
        val res = backendOperator ? "givemeconnection"
        Await.result(res, timeout.duration).asInstanceOf[TTransport]
      }
    }

    override def makeObject(): PooledObject[T] = {
      borrowConn() match {
        case Success(tTransport) => {
          /* it's safe to use 0 as index here because there is
           only one way to construct Thrift Client
          */
          val constructor = manifest.runtimeClass.getConstructors()(0)
          val protocol = new TBinaryProtocol(tTransport)
          val client = constructor.newInstance(protocol)
          new DefaultPooledObject[T](client.asInstanceOf[T])
        }
        case Failure(t) => throw PoolConnectionException(t.getMessage)
      }
    }
  }

}
