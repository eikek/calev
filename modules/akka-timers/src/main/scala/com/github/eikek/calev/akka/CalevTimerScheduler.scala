package com.github.eikek.calev.akka

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{Behaviors, TimerScheduler}
import com.github.eikek.calev.CalEvent
import com.github.eikek.calev.internal.{DefaultTrigger, Trigger}

import java.time.{Clock, ZonedDateTime, Duration => JavaDuration}
import scala.annotation.tailrec
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

  def scheduleUpcoming(calEvent: CalEvent, triggerFactory: ZonedDateTime => T): Unit = {
    def now = clock.instant().atZone(clock.getZone)

    @tailrec def nextEvent(refInstant: ZonedDateTime): Option[(ZonedDateTime, FiniteDuration)] =
      calendar.next(refInstant, calEvent).map { instant =>
        (instant, JavaDuration.between(refInstant, instant).getSeconds.seconds)
      } match {
        case Some((_, duration)) if duration.toMillis < 500 => // avoid loop
          nextEvent(now)
        case result @ Some(_) =>
          result
        case None =>
          Option.empty
      }

    nextEvent(now)
      .foreach { case (instant, delay) =>
        scheduler.startSingleTimer(triggerFactory.apply(instant), delay)
      }
  }

}
