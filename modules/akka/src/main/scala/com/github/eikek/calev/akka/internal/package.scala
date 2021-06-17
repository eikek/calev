package com.github.eikek.calev.akka

import java.util.concurrent.TimeUnit

import scala.concurrent.duration.{Duration, FiniteDuration}

import com.typesafe.config.Config

package object internal {

  implicit final private[akka] class ConfigOps(val config: Config) extends AnyVal {
    def getDurationMillis(path: String): FiniteDuration =
      getDuration(path, TimeUnit.MILLISECONDS)

    def getDurationNanos(path: String): FiniteDuration =
      getDuration(path, TimeUnit.NANOSECONDS)

    private def getDuration(path: String, unit: TimeUnit): FiniteDuration =
      Duration(config.getDuration(path, unit), unit)
  }

}
