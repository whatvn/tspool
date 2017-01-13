package com.pcdn.thriftpool

import org.apache.commons.pool2.impl.{GenericObjectPool, GenericObjectPoolConfig}
import org.apache.thrift.TServiceClient

import scala.collection.mutable.ListBuffer

/**
  * Created by Hung on 1/13/17.
  */

object Pool {

  def apply[T <: TServiceClient : Manifest](poolConfig: GenericObjectPoolConfig, host: String, port: Int): GenericPool[T] = {
    val pool = new Pool[T](poolConfig, host, port)
    val mapConn = ListBuffer[Option[T]]()
    for (_ <- 0 until poolConfig.getMaxTotal) {
      val conn = pool.getConn
      mapConn.+=(conn)
    }
    mapConn.foreach {
      case Some(x) => pool.returnConn(x)
      case None => ()
    }
    pool
  }

  private class Pool[T <: TServiceClient : Manifest](val poolConfig: GenericObjectPoolConfig, val host: String, val port: Int) extends GenericPool[T] {
    override val internalPool: GenericObjectPool[T] = {
      val poolFactory = GenericThriftObjectPool[T](host, port)
      new GenericObjectPool[T](poolFactory, poolConfig)
    }
  }
}