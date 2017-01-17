package com.pcdn.pool

import net.spy.memcached._
import org.apache.commons.pool2.impl.DefaultPooledObject
import org.apache.commons.pool2.{PooledObject, PooledObjectFactory}

/**
  * Created by Hung on 1/17/17.
  */
object GenericMemcachedObjectPool {

  def apply(host: String, port: Int): GenericMemcachedObjectPool = {
    new GenericMemcachedObjectPool(host, port)
  }

  private[pool] class GenericMemcachedObjectPool(host: String, port: Int) extends PooledObjectFactory[MemcachedClient] {
    override def destroyObject(p: PooledObject[MemcachedClient]): Unit = {
      val mc = p.getObject
      mc.shutdown()
    }

    override def validateObject(p: PooledObject[MemcachedClient]): Boolean = {
      val mc = p.getObject
      !mc.getConnection.isShutDown
    }

    override def activateObject(p: PooledObject[MemcachedClient]): Unit = {
      val mc = p.getObject
      mc.asyncGet("SomeThing")
    }

    override def passivateObject(p: PooledObject[MemcachedClient]): Unit = destroyObject(p)

    override def makeObject(): PooledObject[MemcachedClient] = {
      val mc = new MemcachedClient(
        new ConnectionFactoryBuilder(
          new BinaryConnectionFactory()
        ).setDaemon(true).setOpQueueMaxBlockTime(DefaultConnectionFactory.DEFAULT_OP_QUEUE_MAX_BLOCK_TIME / 2)
          .setFailureMode(FailureMode.Retry)
          .build(),
        AddrUtil.getAddresses(s"$host:$port"))
      new DefaultPooledObject[MemcachedClient](mc)
    }
  }
}
