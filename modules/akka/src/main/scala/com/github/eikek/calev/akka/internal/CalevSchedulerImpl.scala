package com.github.eikek.calev.akka.internal

import java.time.Clock

import scala.concurrent.ExecutionContext

import akka.actor.Cancellable
import akka.actor.typed.Scheduler
import com.github.eikek.calev.CalEvent
import com.github.eikek.calev.akka.dsl.CalevScheduler

private[akka] class CalevSchedulerImpl(
    val scheduler: Scheduler,
    clock: Clock = Clock.systemDefaultZone()
) extends CalevScheduler {
  private val upcomingEventProvider = new UpcomingEventProvider(clock)

  def scheduleOnceWithCalendarEvent(calEvent: CalEvent, runnable: Runnable)(implicit
      executor: ExecutionContext
  ): Option[Cancellable] =
    upcomingEventProvider(calEvent).map { case (_, delay) =>
      scheduler.scheduleOnce(delay, runnable)
    }
}
