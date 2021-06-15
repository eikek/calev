package com.github.eikek.calev.akka

import java.time.{Clock, ZonedDateTime}
import java.util.concurrent.TimeUnit

import scala.concurrent.duration.{Duration, FiniteDuration}

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.ActorContext
import com.github.eikek.calev.CalEvent
import com.typesafe.config.Config

object CalevActorScheduling {

  implicit class CalevActorScheduling(ctx: ActorContext[_]) {
    def scheduleUpcoming[T](
        calEvent: CalEvent,
        target: ActorRef[T],
        triggerFactory: ZonedDateTime => T,
        clock: Clock = Clock.systemDefaultZone()
    ): Unit = new UpcomingEventProvider(clock)(calEvent).foreach {
      case (instant, delay) =>
        ctx.scheduleOnce(delay, target, triggerFactory(instant))
    }
  }

  implicit final private[akka] class ConfigOps(val config: Config) extends AnyVal {
    def getDurationMillis(path: String): FiniteDuration =
      getDuration(path, TimeUnit.MILLISECONDS)

    def getDurationNanos(path: String): FiniteDuration =
      getDuration(path, TimeUnit.NANOSECONDS)

    private def getDuration(path: String, unit: TimeUnit): FiniteDuration =
      Duration(config.getDuration(path, unit), unit)
  }

}
