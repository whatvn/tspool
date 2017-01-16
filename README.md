# tspool
Simple Apache thrift connection pool for scala 

*Actually you can use it for any Client service with just a little modification* 



# Usage 

With any thrift `Client` type you generated using thrift definition file, you can use with this pool, we dont have to write/or generate pool for each `Client` type. 

 

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



