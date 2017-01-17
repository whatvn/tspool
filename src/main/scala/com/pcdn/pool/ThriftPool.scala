package com.pcdn.pool

import org.apache.commons.pool2.impl.{GenericObjectPool, GenericObjectPoolConfig}
import org.apache.thrift.TServiceClient

/**
  * Created by Hung on 1/17/17.
  */
object ThriftPool {

  def apply[T <: TServiceClient : Manifest](poolConfig: GenericObjectPoolConfig, host: String, port: Int): Pool[T] = {
    new ThriftPoolImpl[T](poolConfig, host, port)
  }
  // thrift
  private class ThriftPoolImpl[T <: TServiceClient : Manifest](val poolConfig: GenericObjectPoolConfig, val host: String, val port: Int) extends Pool[T] {
    override protected val internalPool: GenericObjectPool[T] = {
      val poolFactory = GenericThriftObjectPool[T](host, port)
      new GenericObjectPool[T](poolFactory, poolConfig)
    }
  }
}