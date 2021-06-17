package com.github.eikek.calev.akka.internal

import java.time.{Clock, ZonedDateTime}

import scala.concurrent.duration.FiniteDuration

import akka.actor.typed.scaladsl.TimerScheduler
import com.github.eikek.calev.CalEvent
import com.github.eikek.calev.akka.dsl.CalevTimerScheduler

private[akka] class CalevTimerSchedulerImpl[T](
    scheduler: TimerScheduler[T],
    clock: Clock,
    minInterval: Option[FiniteDuration]
) extends CalevTimerScheduler[T] {

  private val upcomingEventProvider =
    new UpcomingEventProvider(clock, minInterval)

  def scheduleUpcoming(calEvent: CalEvent, triggerFactory: ZonedDateTime => T): Unit =
    upcomingEventProvider(calEvent)
      .foreach { case (instant, delay) =>
        scheduler.startSingleTimer(triggerFactory.apply(instant), delay)
      }

}
