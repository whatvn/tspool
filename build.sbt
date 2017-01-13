name := "ThriftPool"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= {
  val commonPoolV = "2.4.2"
  val thriftV = "0.9.3"
  Seq(
    "org.apache.commons" % "commons-pool2" % commonPoolV,
    "org.apache.thrift" % "libthrift" % thriftV
  )
}
