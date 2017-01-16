package com.pcdn.thriftpool

import org.apache.commons.pool2.impl.DefaultPooledObject
import org.apache.commons.pool2.{PooledObject, PooledObjectFactory}
import org.apache.thrift.TServiceClient
import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.transport.{TFramedTransport, TSocket, TTransportFactory}

/**
  * Created by Hung on 1/12/17.
  *
  *
  */
object GenericThriftObjectPool {
  def apply[T <: TServiceClient : Manifest](host: String, port: Int): GenericThriftObjectPool[T] =
    new GenericThriftObjectPool[T](host, port)

  private[thriftpool] class GenericThriftObjectPool[T <: TServiceClient : Manifest](val host: String,
                                                                                    val port: Int)
    extends PooledObjectFactory[T] {


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

    override def makeObject(): PooledObject[T] = {
      val tTransportFactory = new TTransportFactory()
      val tFramedTransport = new TFramedTransport(new TSocket(host,
        port, 200))
      val transport = tTransportFactory.getTransport(tFramedTransport)
      transport.open()
      println(manifest.runtimeClass.toGenericString)
      val constructor = manifest.runtimeClass.getConstructors()(0)
      val protocol = new TBinaryProtocol(transport)
      val client = constructor.newInstance(protocol)
      new DefaultPooledObject[T](client.asInstanceOf[T])
    }
  }

}
