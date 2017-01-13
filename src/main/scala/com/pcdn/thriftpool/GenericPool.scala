package com.pcdn.thriftpool

import org.apache.commons.pool2.impl.GenericObjectPool

/**
  * Created by Hung on 1/12/17.
  */
abstract class GenericPool[T]  {
  val internalPool : GenericObjectPool[T]

  def getConn: Option[T] = internalPool match  {
    case null => None
    case _ =>   Some(internalPool.borrowObject())
  }

  def returnConn(conn: T): Unit = internalPool match {
    case null => ()
    case _ => internalPool.returnObject(conn)
  }

  def returnBrokenConn(conn: T): Unit = {
    if (conn != null) {
      internalPool match  {
        case null => ()
        case _ => internalPool.invalidateObject(conn)
      }
    }
  }

  def destroy: Unit = internalPool match {
    case null => ()
    case _ => internalPool.close()
  }
}
