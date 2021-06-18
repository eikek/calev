package com.github.eikek.calev.akka.dsl

import java.time.{Clock, ZonedDateTime}

import akka.actor.Cancellable
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.ActorContext
import com.github.eikek.calev.CalEvent
import com.github.eikek.calev.akka.internal.UpcomingEventProvider

trait CalevActorScheduling {

  implicit class CalevActorSchedulingDsl(ctx: ActorContext[_]) {

    /** Schedule the sending of a message to the given target Actor
      * at the time of the upcoming event according to the given
      * calendar event definition.
      *
      * The scheduled action can be cancelled
      * by invoking Cancellable#cancel on the returned
      * handle.
      *
      * This method is thread-safe and can be called from other threads than the ordinary
      * actor message processing thread, such as Future callbacks.
      */
    def scheduleOnceWithCalendarEvent[T](
        calEvent: CalEvent,
        target: ActorRef[T],
        msgFactory: ZonedDateTime => T,
        clock: Clock = Clock.systemDefaultZone()
    ): Option[Cancellable] = new UpcomingEventProvider(clock)(calEvent).map {
      case (instant, delay) =>
        ctx.scheduleOnce(delay, target, msgFactory(instant))
    }
  }

}
