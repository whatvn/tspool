package com.pcdn.pool

import net.spy.memcached.MemcachedClient
import org.apache.commons.pool2.impl.{GenericObjectPool, GenericObjectPoolConfig}

/**
  * Created by Hung on 1/17/17.
  */
object MemcachedPool {

  def apply[T <: MemcachedClient](poolConfig: GenericObjectPoolConfig, host: String, port: Int): Pool[MemcachedClient] = {
    new MemcachedPoolImpl[MemcachedClient](poolConfig, host, port)
  }
  // memcached
  private class MemcachedPoolImpl[T <: MemcachedClient](val poolConfig: GenericObjectPoolConfig, val host: String, val port: Int) extends Pool[MemcachedClient] {
    override protected val internalPool: GenericObjectPool[MemcachedClient] = {
      val poolFactory = GenericMemcachedObjectPool(host, port)
      new GenericObjectPool(poolFactory, poolConfig)
    }
  }

}