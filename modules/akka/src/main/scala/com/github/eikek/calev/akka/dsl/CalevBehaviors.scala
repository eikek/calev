package com.github.eikek.calev.akka.dsl

import java.time.{Clock, ZonedDateTime}

import scala.concurrent.duration.FiniteDuration
import scala.reflect.ClassTag

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.github.eikek.calev.CalEvent
import com.github.eikek.calev.akka.internal.{CalevInterceptor, CalevTimerSchedulerImpl}

object CalevBehaviors {

  def withCalevTimers[T](
      minInterval: Option[FiniteDuration] = None,
      clock: Clock = Clock.systemDefaultZone()
  )(factory: CalevTimerScheduler[T] => Behavior[T]): Behavior[T] =
    Behaviors.withTimers { scheduler =>
      factory(new CalevTimerSchedulerImpl[T](scheduler, clock, minInterval))
    }

  def withCalendarEvent[I, O <: I: ClassTag](
      calEvent: CalEvent,
      clock: Clock = Clock.systemDefaultZone()
  )(triggerFactory: ZonedDateTime => O, inner: Behavior[I]): Behavior[I] =
    Behaviors
      .intercept(() => new CalevInterceptor[I, O](clock, calEvent, triggerFactory))(inner)
      .asInstanceOf[Behavior[I]]

}
