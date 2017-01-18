name := "ThriftPool"

version := "1.0"

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8")

scalaVersion := "2.11.8"

libraryDependencies ++= {
  val commonPoolV = "2.4.2"
  val thriftV = "0.9.3"
  val spymemcachedV = "2.12.1"
  val akkaV = "2.4.16"
  Seq(
    "org.apache.commons" % "commons-pool2" % commonPoolV,
    "org.apache.thrift" % "libthrift" % thriftV,
    "net.spy" % "spymemcached" % spymemcachedV,
    "com.typesafe.akka" %% "akka-actor" % akkaV
  )
}
