package com.github.eikek.calev.akka

import akka.actor.typed.Scheduler
import com.github.eikek.calev.CalEvent

import java.time.Clock
import scala.concurrent.ExecutionContext

class CalevScheduler(scheduler: Scheduler, clock: Clock = Clock.systemDefaultZone()) {
  private val upcomingEventProvider = new UpcomingEventProvider(clock)

  def scheduleUpcoming(calEvent: CalEvent, runnable: Runnable)(implicit executor: ExecutionContext): Unit =
    upcomingEventProvider(calEvent).foreach {
      case (_, delay) =>
        scheduler.scheduleOnce(delay, runnable)
    }
}
