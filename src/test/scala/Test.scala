import com.pcdn.pool.{MemcachedPool, ThriftPool}
import net.spy.memcached.MemcachedClient
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import thrift.PlusMe.Client

/**
  * Created by Hung on 1/12/17.
  */
object Test {

  def main(args: Array[String]): Unit = {
    val poolConfig = new GenericObjectPoolConfig
    poolConfig.setMaxTotal(10)
    poolConfig.setMinIdle(5)
    val pool = ThriftPool[Client](poolConfig, "127.0.0.1", 9998)
    val res: Int = pool.withClient(c => {
      c.plusMe(1, 2)
    })
    println(res)

    val mcPool = MemcachedPool[MemcachedClient](poolConfig, "192.168.10.224", 11212)
    val res1: AnyRef = mcPool.withClient(c => {
      c.set("Hello", 86400, "World")
      c.get("Hello")
    })
    println(res1.toString)
  }


}
