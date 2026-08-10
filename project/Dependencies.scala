import sbt._

object Dependencies {

  val akkaVersion = "2.6.20"
  val circeVersion = "0.14.15"
  val doobieVersion = "1.0.0-RC10"
  val fs2Version = "3.12.2"
  val h2Version = "2.4.240"
  val jacksonVersion = "2.20.0"
  val log4sVersion = "1.8.2"
  val logbackVersion = "1.5.32"
  val munitVersion = "1.2.0"
  val scalaTestVersion = "3.2.19"

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
