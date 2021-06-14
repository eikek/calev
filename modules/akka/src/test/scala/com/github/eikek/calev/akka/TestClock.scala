package com.github.eikek.calev.akka

import java.time._
import scala.compat.java8.DurationConverters.FiniteDurationops
import scala.concurrent.duration.FiniteDuration

class TestClock extends Clock {

  @volatile private var _instant = roundToMillis(Instant.now())

  override def getZone: ZoneId = ZoneOffset.UTC

  override def withZone(zone: ZoneId): Clock =
    throw new UnsupportedOperationException("withZone not supported")

  override def instant(): Instant =
    _instant

  def setInstant(newInstant: Instant): Unit =
    _instant = roundToMillis(newInstant)

  def tick(duration: FiniteDuration): Instant =
    tick(duration.toJava)

  def tick(duration: Duration): Instant = {
    val newInstant = roundToMillis(_instant.plus(duration))
    _instant = newInstant
    newInstant
  }

  private def roundToMillis(i: Instant): Instant = {
    // algo taken from java.time.Clock.tick
    val epochMilli = i.toEpochMilli
    Instant.ofEpochMilli(epochMilli - Math.floorMod(epochMilli, 1L))
  }

  override def toString =
    s"TestClock(${_instant})"
}
