# tspool
Simple Apache thrift connection pool for scala 

*Actually you can use it for any Client service with just a little modification, see example memcached pool implemetation inside this project* 



# Usage 

With any thrift `Client` type you generated using thrift definition file, you can use with this pool, we dont have to write/or generate pool for each `Client` type. 




### thift 

```scala
val poolConfig = new GenericObjectPoolConfig
poolConfig.setMaxTotal(10)
poolConfig.setMinIdle(5)
val pool = Pool[Client](poolConfig, "127.0.0.1", 9998)
val res: Int = pool.withClient(c => {
  c.plusMe(1, 2)
})
println(res)
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
