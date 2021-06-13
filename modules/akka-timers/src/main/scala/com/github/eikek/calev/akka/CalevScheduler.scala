package com.github.eikek.calev.akka

import akka.actor.typed.Scheduler
import com.github.eikek.calev.CalEvent
import com.github.eikek.calev.internal.{DefaultTrigger, Trigger}

import java.time.Clock
import scala.concurrent.ExecutionContext

class CalevScheduler(scheduler: Scheduler, calendar: Trigger = DefaultTrigger, clock: Clock = Clock.systemDefaultZone()) {
  private val upcomingEventProvider = new UpcomingEventProvider(calendar, clock)

  def scheduleUpcoming(calEvent: CalEvent, runnable: Runnable)(implicit executor: ExecutionContext): Unit =
    upcomingEventProvider(calEvent).foreach {
      case (_, delay) =>
        scheduler.scheduleOnce(delay, runnable)
    }
}
