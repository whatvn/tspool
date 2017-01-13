import com.pcdn.thriftpool.Pool
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import thrift.PlusMe.Client

/**
  * Created by Hung on 1/12/17.
  */
object Test {

  def main(args: Array[String]): Unit = {

    val poolConfig = new GenericObjectPoolConfig
    poolConfig.setMaxTotal(200)
    poolConfig.setMinIdle(100)
    val pool = Pool[Client](poolConfig, "127.0.0.1", 9998)
    val client: Option[Client] = pool.getConn
    client match {
      case Some(c) => println(c.plusMe(1, 2))
      case None => println("connection error")
    }
  }
}
