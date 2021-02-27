import com.typesafe.sbt.SbtGit.GitKeys._
import xerial.sbt.Sonatype._
import ReleaseTransformations._

val scala212 = "2.12.13"
val scala213 = "2.13.2"

val updateReadme = inputKey[Unit]("Update readme")

val sharedSettings = Seq(
  organization := "com.github.eikek",
  scalaVersion := scala213,
  scalacOptions ++=
    Seq("-feature", "-deprecation", "-unchecked", "-encoding", "UTF-8", "-language:higherKinds") ++
      (if (scalaBinaryVersion.value.startsWith("2.12"))
         List(
           "-Xfatal-warnings", // fail when there are warnings
           "-Xlint",
           "-Yno-adapted-args",
           "-Ywarn-dead-code",
           "-Ywarn-unused-import",
           "-Ypartial-unification",
           "-Ywarn-value-discard"
         )
       else if (scalaBinaryVersion.value.startsWith("2.13"))
         List("-Werror", "-Wdead-code", "-Wunused", "-Wvalue-discard")
       else
         Nil),
  crossScalaVersions := Seq(scala212, scala213),
  scalacOptions in Test := Seq(),
  scalacOptions in (Compile, console) := Seq(),
  licenses := Seq("MIT" -> url("http://spdx.org/licenses/MIT")),
  homepage := Some(url("https://github.com/eikek/calev"))
) ++ publishSettings

lazy val publishSettings = Seq(
  publishTo := sonatypePublishToBundle.value,
  publishMavenStyle := true,
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/eikek/calev.git"),
      "scm:git:git@github.com:eikek/calev.git"
    )
  ),
  developers := List(
    Developer(
      id = "eikek",
      name = "Eike Kettner",
      url = url("https://github.com/eikek"),
      email = ""
    )
  ),
  publishArtifact in Test := false,
  releaseCrossBuild := true,
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    //For non cross-build projects, use releaseStepCommand("publishSigned")
    releaseStepCommandAndRemaining("+publishSigned"),
    releaseStepCommand("sonatypeBundleRelease"),
    setNextVersion,
    commitNextVersion,
    pushChanges
  ),
  sonatypeProjectHosting := Some(GitHubHosting("eikek", "calev", "eike.kettner@posteo.de"))
)

lazy val noPublish = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

val testSettings = Seq(
  testFrameworks += new TestFramework("minitest.runner.Framework"),
  libraryDependencies ++=
    (Dependencies.miniTest ++
      Dependencies.logback).map(_ % Test)
)

val buildInfoSettings = Seq(
  buildInfoKeys := Seq[BuildInfoKey](
    name,
    version,
    scalaVersion,
    sbtVersion,
    gitHeadCommit,
    gitHeadCommitDate,
    gitUncommittedChanges,
    gitDescribedVersion
  ),
  buildInfoOptions += BuildInfoOption.ToJson,
  buildInfoOptions += BuildInfoOption.BuildTime
)

lazy val core = project
  .in(file("modules/core"))
  .settings(sharedSettings)
  .settings(testSettings)
  .settings(
    name := "calev-core",
    libraryDependencies ++=
      Dependencies.fs2.map(_ % Test) ++
      Dependencies.fs2io.map(_ % Test)
  )

lazy val fs2 = project
  .in(file("modules/fs2"))
  .settings(sharedSettings)
  .settings(testSettings)
  .settings(
    name := "calev-fs2",
    libraryDependencies ++=
      Dependencies.fs2
  )
  .dependsOn(core)

lazy val doobie = project
  .in(file("modules/doobie"))
  .settings(sharedSettings)
  .settings(testSettings)
  .settings(
    name := "calev-doobie",
    libraryDependencies ++=
      Dependencies.doobie ++
      Dependencies.h2.map(_ % Test)
  )
  .dependsOn(core)

lazy val circe = project
  .in(file("modules/circe"))
  .settings(sharedSettings)
  .settings(testSettings)
  .settings(
    name := "calev-circe",
    libraryDependencies ++=
      Dependencies.circe ++
      Dependencies.circeAll.map(_ % Test)
  )
  .dependsOn(core)


lazy val readme = project
  .in(file("modules/readme"))
  .enablePlugins(MdocPlugin)
  .settings(sharedSettings)
  .settings(noPublish)
  .settings(
    name := "calev-readme",
    libraryDependencies ++=
      Dependencies.circeAll,
    scalacOptions := Seq(),
    mdocVariables := Map(
      "VERSION" -> version.value
    ),
    updateReadme := {
      mdoc.evaluated
      val out = mdocOut.value / "readme.md"
      val target = (LocalRootProject / baseDirectory).value / "README.md"
      val logger = streams.value.log
      logger.info(s"Updating readme: $out -> $target")
      IO.copyFile(out, target)
      ()
    }
  )
  .dependsOn(core, fs2, doobie, circe)

val root = project
  .in(file("."))
  .settings(sharedSettings)
  .settings(noPublish)
  .settings(
    name := "calev-root"
  )
  .aggregate(core, fs2, doobie, circe)
