package com.github.eikek.calev.akka

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.ActorContext
import com.github.eikek.calev.CalEvent
import com.github.eikek.calev.internal.{DefaultTrigger, Trigger}

import java.time.{Clock, ZonedDateTime}

object CalevActorScheduling {
  implicit class CalevActorScheduling(ctx: ActorContext[_]) {
    def scheduleUpcoming[T](
        calEvent: CalEvent,
        target: ActorRef[T],
        triggerFactory: ZonedDateTime => T,
        calendar: Trigger = DefaultTrigger,
        clock: Clock = Clock.systemDefaultZone()
    ): Unit = new UpcomingEventProvider(calendar, clock)(calEvent).foreach {
      case (instant, delay) =>
        ctx.scheduleOnce(delay, target, triggerFactory(instant))
    }
  }
}
