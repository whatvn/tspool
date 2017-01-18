# tspool
Simple Apache thrift connection pool for scala 

- it takes a list of `Server` to load balance request between servers (random)
- if for any reason one or more servers cannot response, these server will be remove from active servers list, but active server is still be using to perform operation 
- a scheduler will periodically check dead server list to see if they are alive again, then add it to active server list 
- if all servers are dead, `PoolConnectionException` will be raised 


# implementation 

- Connection pool built on Apache common pool 2 
- Connection, server checking... uses akka actor
- addtional: memcached pool example uses spymemcached client  


*Actually you can use it for any Client service with just a little modification, see example memcached pool implemetation inside this project* 



# Usage 

With any thrift `Client` type you generated using thrift definition file, you can use with this pool, we dont have to write/or generate pool for each `Client` type. 




### thift 

```scala
val poolConfig = new GenericObjectPoolConfig
poolConfig.setMaxTotal(10)
poolConfig.setMinIdle(5)
val server = Server("127.0.0.1", 9999)
val server1 = Server("127.0.0.1", 9998)
val pool = ThriftPool[Client](poolConfig, Servers(List(server, server1)))
try {
  val res: Int = pool.withClient(c => {
    c.plusMe(1, 2)
  })
  println(res)
} catch {
  case x: PoolConnectionException => println(x.getMessage)
}
```


### memcached


```scala 
val poolConfig = new GenericObjectPoolConfig
poolConfig.setMaxTotal(10)
poolConfig.setMinIdle(5)
val mcPool = MemcachedPool[MemcachedClient](poolConfig, "192.168.10.224", 11212)
val res1: AnyRef = mcPool.withClient(c => {
   c.set("Hello", 86400, "World")
   c.get("Hello")
})
println(res1.toString)
```
