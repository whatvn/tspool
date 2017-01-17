package com.pcdn.pool

import org.apache.commons.pool2.impl.GenericObjectPool

/**
  * Created by Hung on 1/13/17.
  */

abstract class Pool[T] {
  protected val internalPool: GenericObjectPool[T]

  def getConn: T = internalPool.borrowObject()

  def returnConn(conn: T): Unit = internalPool match {
    case null => ()
    case _ => internalPool.returnObject(conn)
  }

  def returnBrokenConn(conn: T): Unit = {
    if (conn != null) {
      internalPool match {
        case null => ()
        case _ => internalPool.invalidateObject(conn)
      }
    }
  }

  def destroy(): Unit = internalPool match {
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



