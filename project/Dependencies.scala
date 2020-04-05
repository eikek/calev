import sbt._

object Dependencies {

  val doobieVersion = "0.8.8"
  val fs2Version = "2.3.0"
  val h2Version = "1.4.200"
  val log4sVersion = "1.8.2"
  val logbackVersion = "1.2.3"
  val miniTestVersion = "2.8.1"

  val fs2 = Seq(
    "co.fs2" %% "fs2-core" % fs2Version
  )

  val fs2io = Seq(
    "co.fs2" %% "fs2-io" % fs2Version
  )

  val miniTest = Seq(
    // https://github.com/monix/minitest
    // Apache 2.0
    "io.monix" %% "minitest" % miniTestVersion,
    "io.monix" %% "minitest-laws" % miniTestVersion
  )

  // https://github.com/Log4s/log4s;ASL 2.0
  val loggingApi = Seq(
    "org.log4s" %% "log4s" % log4sVersion
  )

  val logback = Seq(
    "ch.qos.logback" % "logback-classic" % logbackVersion
  )

  val doobie = Seq(
    "org.tpolecat" %% "doobie-core" % doobieVersion
  )
  val h2 = Seq(
    "com.h2database" % "h2" % h2Version
  )

}
