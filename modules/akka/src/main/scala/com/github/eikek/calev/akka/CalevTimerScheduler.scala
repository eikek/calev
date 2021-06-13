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
      clock: Clock
  )(factory: CalevTimerScheduler[T] => Behavior[T]): Behavior[T] =
    Behaviors.withTimers { scheduler =>
      factory(new CalevTimerSchedulerImpl[T](scheduler, DefaultTrigger, clock))
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
    clock: Clock
) extends CalevTimerScheduler[T] {

  private val upcomingEventProvider = new UpcomingEventProvider(calendar, clock, minInterval = Some(500.millis))

  def scheduleUpcoming(calEvent: CalEvent, triggerFactory: ZonedDateTime => T): Unit =
    upcomingEventProvider(calEvent)
      .foreach { case (instant, delay) =>
        scheduler.startSingleTimer(triggerFactory.apply(instant), delay)
      }

}
