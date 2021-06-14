package com.github.eikek.calev.akka

import com.github.eikek.calev.CalEvent

import java.time.{Clock, ZonedDateTime, Duration => JavaDuration}
import scala.concurrent.duration._

class UpcomingEventProvider(
    clock: Clock,
    minInterval: Option[FiniteDuration] = None
) {
  private def now = clock.instant().atZone(clock.getZone)

  def apply(calEvent: CalEvent, delay: FiniteDuration = Duration.Zero): Option[(ZonedDateTime, FiniteDuration)] = {
    val refInstant = now.minusNanos(delay.toNanos)
    calEvent.nextElapse(refInstant)
      .map { instant =>
        (instant, JavaDuration.between(refInstant, instant).toMillis.millis)
      }
      .flatMap {
        case (_, duration) if minInterval.exists(min => duration.toMillis < min.toMillis) =>
          apply(calEvent, minInterval.get)
        case (dt, duration) =>
          Some(dt, duration)
      }
  }

}
