package com.pcdn.pool

case class Server(host: String, port: Int)
case class Servers(info: List[Server])

case class CheckMessage(op: Server => Boolean)