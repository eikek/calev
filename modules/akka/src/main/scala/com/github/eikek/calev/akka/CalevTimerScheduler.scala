package com.github.eikek.calev.akka

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{Behaviors, TimerScheduler}
import com.github.eikek.calev.CalEvent
import com.github.eikek.calev.internal.{DefaultTrigger, Trigger}

import java.time.{Clock, ZonedDateTime}
import scala.concurrent.duration._
import scala.reflect.ClassTag

object CalevTimerScheduler {

  def withCalevTimers[T](
      clock: Clock,
      minInterval: Option[FiniteDuration] = None
  )(factory: CalevTimerScheduler[T] => Behavior[T]): Behavior[T] =
    Behaviors.withTimers { scheduler =>
      factory(new CalevTimerSchedulerImpl[T](scheduler, DefaultTrigger, clock, minInterval))
    }

  def withCalendarEvent[B, T <: B: ClassTag](
      clock: Clock,
      calEvent: CalEvent,
      triggerFactory: ZonedDateTime => T
  )(inner: Behavior[B]): Behavior[T] =
    Behaviors.intercept(() =>
      new CalevInterceptor[B, T](clock, calEvent, triggerFactory)
    )(inner)

}

trait CalevTimerScheduler[T] {
  def scheduleUpcoming(calEvent: CalEvent, triggerFactory: ZonedDateTime => T): Unit
}

private[akka] class CalevTimerSchedulerImpl[T](
    scheduler: TimerScheduler[T],
    calendar: Trigger,
    clock: Clock,
    minInterval: Option[FiniteDuration]
) extends CalevTimerScheduler[T] {

  private val upcomingEventProvider =
    new UpcomingEventProvider(calendar, clock, minInterval)

  def scheduleUpcoming(calEvent: CalEvent, triggerFactory: ZonedDateTime => T): Unit =
    upcomingEventProvider(calEvent)
      .foreach { case (instant, delay) =>
        scheduler.startSingleTimer(triggerFactory.apply(instant), delay)
      }

}
