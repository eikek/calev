package com.github.eikek.calev.akka.dsl

import scala.concurrent.ExecutionContext

import akka.actor.Cancellable
import com.github.eikek.calev.CalEvent

trait CalevScheduler {
  def scheduleUpcoming(calEvent: CalEvent, runnable: Runnable)(implicit
      executor: ExecutionContext
  ): Option[Cancellable]
}
