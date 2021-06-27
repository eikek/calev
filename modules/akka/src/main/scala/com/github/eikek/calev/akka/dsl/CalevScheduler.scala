package com.github.eikek.calev.akka.dsl

import scala.concurrent.ExecutionContext

import _root_.akka.actor.Cancellable
import com.github.eikek.calev.CalEvent

trait CalevScheduler {

  /** Schedules a Runnable to be run at a time of the upcoming
    * event according to the given calendar event definition.
    *
    * Throws IllegalArgumentException if the calculated delay exceed the maximum
    * reach (calculated as: `delay / tickNanos > Int.MaxValue`).
    */
  def scheduleOnceWithCalendarEvent(calEvent: CalEvent, runnable: Runnable)(implicit
      executor: ExecutionContext
  ): Option[Cancellable]
}
