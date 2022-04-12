import com.typesafe.sbt.SbtGit.GitKeys._

val scala212 = "2.12.15"
val scala213 = "2.13.8"
val scala3 = "3.1.2"

val updateReadme = inputKey[Unit]("Update readme")

addCommandAlias("ci", "; lint; +test; readme/updateReadme; +publishLocal")
addCommandAlias(
  "lint",
  "; scalafmtSbtCheck; scalafmtCheckAll; Compile/scalafix --check; Test/scalafix --check"
)
addCommandAlias("fix", "; Compile/scalafix; Test/scalafix; scalafmtSbt; scalafmtAll")

val sharedSettings = Seq(
  organization := "com.github.eikek",
  scalaVersion := scala213,
  scalacOptions ++=
    Seq(
      "-feature",
      "-deprecation",
      "-unchecked",
      "-encoding",
      "UTF-8",
      "-language:higherKinds"
    ) ++
      (if (scalaBinaryVersion.value.startsWith("2.12"))
         List(
           "-Xfatal-warnings", // fail when there are warnings
           "-Xlint",
           "-Yno-adapted-args",
           "-Ywarn-dead-code",
           "-Ywarn-unused",
           "-Ypartial-unification",
           "-Ywarn-value-discard"
         )
       else if (scalaBinaryVersion.value.startsWith("2.13"))
         List("-Werror", "-Wdead-code", "-Wunused", "-Wvalue-discard")
       else if (scalaBinaryVersion.value.startsWith("3"))
         List(
           "-explain",
           "-explain-types",
           "-indent",
           "-print-lines",
           "-Ykind-projector",
           "-Xmigration",
           "-Xfatal-warnings"
         )
       else
         Nil),
  crossScalaVersions := Seq(scala212, scala213, scala3),
  Test / console / scalacOptions := Seq(),
  Compile / console / scalacOptions := Seq(),
  licenses := Seq("MIT" -> url("http://spdx.org/licenses/MIT")),
  homepage := Some(url("https://github.com/eikek/calev")),
  versionScheme := Some("early-semver")
) ++ publishSettings

lazy val publishSettings = Seq(
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
  Test / publishArtifact := false
)

lazy val noPublish = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

val testSettings = Seq(
  libraryDependencies ++= (Dependencies.munit ++ Dependencies.logback).map(_ % Test),
  testFrameworks += TestFrameworks.MUnit
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

val scalafixSettings = Seq(
  semanticdbEnabled := true, // enable SemanticDB
  semanticdbVersion := scalafixSemanticdb.revision, // use Scalafix compatible version
  ThisBuild / scalafixDependencies ++= Dependencies.organizeImports
)

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .withoutSuffixFor(JVMPlatform)
  .in(file("modules/core"))
  .settings(sharedSettings)
  .settings(testSettings)
  .settings(scalafixSettings)
  .settings(
    name := "calev-core",
    libraryDependencies ++=
      Dependencies.fs2.map(_ % Test) ++
        Dependencies.fs2io.map(_ % Test)
  )

lazy val fs2 = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .withoutSuffixFor(JVMPlatform)
  .in(file("modules/fs2"))
  .settings(sharedSettings)
  .settings(testSettings)
  .settings(scalafixSettings)
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
  .settings(scalafixSettings)
  .settings(
    name := "calev-doobie",
    libraryDependencies ++=
      Dependencies.doobie ++
        Dependencies.h2.map(_ % Test)
  )
  .dependsOn(core.jvm)

lazy val circe = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .withoutSuffixFor(JVMPlatform)
  .in(file("modules/circe"))
  .settings(sharedSettings)
  .settings(testSettings)
  .settings(scalafixSettings)
  .settings(
    name := "calev-circe",
    libraryDependencies ++=
      Dependencies.circe ++
        Dependencies.circeAll.map(_ % Test)
  )
  .dependsOn(core)

lazy val jackson = project
  .in(file("modules/jackson"))
  .dependsOn(core.jvm)
  .settings(sharedSettings)
  .settings(testSettings)
  .settings(scalafixSettings)
  .settings(
    name := "calev-jackson",
    crossScalaVersions := Seq(scala212, scala213),
    developers += Developer(
      id = "pawelkaczor",
      name = "Paweł Kaczor",
      url = url("https://github.com/pawelkaczor"),
      email = ""
    ),
    libraryDependencies ++=
      Dependencies.jacksonAll
  )

lazy val akka = project
  .in(file("modules/akka"))
  .dependsOn(core.jvm)
  .settings(sharedSettings)
  .settings(scalafixSettings)
  .settings(
    name := "calev-akka",
    crossScalaVersions := Seq(scala212, scala213),
    developers += Developer(
      id = "pawelkaczor",
      name = "Paweł Kaczor",
      url = url("https://github.com/pawelkaczor"),
      email = ""
    ),
    libraryDependencies ++=
      Dependencies.akkaAll ++ Dependencies.scalaTest ++ Dependencies.logback.map(_ % Test)
  )

lazy val readme = project
  .in(file("modules/readme"))
  .enablePlugins(MdocPlugin)
  .settings(sharedSettings)
  .settings(noPublish)
  .settings(
    name := "calev-readme",
    crossScalaVersions := Seq(scala212, scala213),
    libraryDependencies ++=
      Dependencies.circeAll,
    scalacOptions -= "-Xfatal-warnings",
    scalacOptions -= "-Werror",
    mdocVariables := Map(
      "VERSION" -> latestRelease.value
    ),
    mdocIn := (LocalRootProject / baseDirectory).value / "docs" / "readme.md",
    mdocOut := (LocalRootProject / baseDirectory).value / "README.md",
    fork := true,
    updateReadme := {
      mdoc.evaluated
      ()
    }
  )
  .dependsOn(core.jvm, fs2.jvm, doobie, circe.jvm, jackson, akka)

val root = project
  .in(file("."))
  .settings(sharedSettings)
  .settings(noPublish)
  .settings(
    name := "calev-root",
    crossScalaVersions := Nil
  )
  .aggregate(
    core.jvm,
    core.js,
    fs2.jvm,
    fs2.js,
    doobie,
    circe.jvm,
    circe.js,
    jackson,
    akka
  )
