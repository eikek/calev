package com.github.eikek.calev.akka.dsl

import java.time.{Clock, ZonedDateTime}

import scala.concurrent.duration.FiniteDuration
import scala.reflect.ClassTag

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.github.eikek.calev.CalEvent
import com.github.eikek.calev.akka.internal.{CalevInterceptor, CalevTimerSchedulerImpl}

object CalevBehaviors {

  /** Support for scheduled `self` messages in an actor. It takes care of the lifecycle of
    * the timers such as cancelling them when the actor is restarted or stopped.
    *
    * @see
    *   [[CalevTimerScheduler]]
    */
  def withCalevTimers[T](
      minInterval: Option[FiniteDuration] = None,
      clock: Clock = Clock.systemDefaultZone()
  )(factory: CalevTimerScheduler[T] => Behavior[T]): Behavior[T] =
    Behaviors.withTimers { scheduler =>
      factory(new CalevTimerSchedulerImpl[T](scheduler, clock, minInterval))
    }

  /** Schedule the sending of a message at a time of the upcoming event according to the
    * given calendar event definition.
    */
  def withCalendarEvent[I, O <: I: ClassTag](
      calEvent: CalEvent,
      clock: Clock = Clock.systemDefaultZone()
  )(msgFactory: ZonedDateTime => O, inner: Behavior[I]): Behavior[I] =
    Behaviors
      .intercept(() => new CalevInterceptor[I, O](clock, calEvent, msgFactory))(inner)
      .asInstanceOf[Behavior[I]]

}
