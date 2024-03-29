import sbt._

object Dependencies {

  val akkaVersion = "2.6.20"
  val circeVersion = "0.14.6"
  val doobieVersion = "1.0.0-RC5"
  val fs2Version = "3.10.2"
  val h2Version = "2.2.224"
  val jacksonVersion = "2.17.0"
  val log4sVersion = "1.8.2"
  val logbackVersion = "1.5.3"
  val munitVersion = "0.7.29"
  val scalaTestVersion = "3.2.18"

  val scalaTest = Seq(
    "org.scalatest" %% "scalatest" % scalaTestVersion % Test
  )

  val munit = Seq(
    "org.scalameta" %% "munit" % munitVersion,
    "org.scalameta" %% "munit-scalacheck" % munitVersion
  )

  val fs2 = Seq(
    "co.fs2" %% "fs2-core" % fs2Version
  )

  val fs2io = Seq(
    "co.fs2" %% "fs2-io" % fs2Version
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

  val circe = Seq(
    "io.circe" %% "circe-core" % circeVersion
  )
  val circeAll = Seq(
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion
  )

  val jacksonAll = Seq(
    "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion,
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion % Test
  )

  val akkaAll = Seq(
    "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
    "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test
  )
}
