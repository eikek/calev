package com.github.eikek.calev.akka.dsl

import java.time.ZonedDateTime

import com.github.eikek.calev.CalEvent

trait CalevTimerScheduler[T] {
  def scheduleUpcoming(calEvent: CalEvent, triggerFactory: ZonedDateTime => T): Unit
}
