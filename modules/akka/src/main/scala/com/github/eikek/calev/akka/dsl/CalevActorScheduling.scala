package com.github.eikek.calev.akka.dsl

import java.time.{Clock, ZonedDateTime}

import akka.actor.Cancellable
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.ActorContext
import com.github.eikek.calev.CalEvent
import com.github.eikek.calev.akka.internal.UpcomingEventProvider

trait CalevActorScheduling {

  implicit class CalevActorSchedulingDsl(ctx: ActorContext[_]) {
    def scheduleUpcoming[T](
        calEvent: CalEvent,
        target: ActorRef[T],
        triggerFactory: ZonedDateTime => T,
        clock: Clock = Clock.systemDefaultZone()
    ): Option[Cancellable] = new UpcomingEventProvider(clock)(calEvent).map {
      case (instant, delay) =>
        ctx.scheduleOnce(delay, target, triggerFactory(instant))
    }
  }

}
