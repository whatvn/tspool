package com.pcdn.thriftpool

import org.apache.commons.pool2.impl.{GenericObjectPool, GenericObjectPoolConfig}
import org.apache.thrift.TServiceClient

/**
  * Created by Hung on 1/13/17.
  */

trait Pool[T] {
  protected val internalPool: GenericObjectPool[T]

  private def getConn: T = internalPool.borrowObject()

  private def returnConn(conn: T): Unit = internalPool match {
    case null => ()
    case _ => internalPool.returnObject(conn)
  }

  private def returnBrokenConn(conn: T): Unit = {
    if (conn != null) {
      internalPool match {
        case null => ()
        case _ => internalPool.invalidateObject(conn)
      }
    }
  }

  private def destroy(): Unit = internalPool match {
    case null => ()
    case _ => internalPool.close()
  }

  def withClient[U](op: T => U): U = {
    val conn = getConn
    try {
      op(conn)
    } finally {
      returnConn(conn)
    }
  }
}

object Pool {

  def apply[T <: TServiceClient : Manifest](poolConfig: GenericObjectPoolConfig, host: String, port: Int): Pool[T] = {
    new PoolImpl[T](poolConfig, host, port)
  }

  private class PoolImpl[T <: TServiceClient : Manifest](val poolConfig: GenericObjectPoolConfig, val host: String, val port: Int) extends Pool[T] {
    override protected val internalPool: GenericObjectPool[T] = {
      val poolFactory = GenericThriftObjectPool[T](host, port)
      new GenericObjectPool[T](poolFactory, poolConfig)
    }
  }

}