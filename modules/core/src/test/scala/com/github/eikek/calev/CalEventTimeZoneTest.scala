package com.github.eikek.calev

import com.github.eikek.calev.Dsl._
import munit._

import java.time.{ZoneId, ZonedDateTime}

class CalEventTimeZoneTest extends FunSuite {

  test("next elapse with time zone") {
    val refInUtc: ZonedDateTime =
      ZonedDateTime
        .of(2021, 11, 15, 11, 15, 0, 0, ZoneId.of("CET"))
        .withZoneSameInstant(ZoneId.of("UTC"))

    assertEquals(refInUtc.getHour, 10)
    assertEquals(refInUtc.getZone, ZoneId.of("UTC"))

    val ce = CalEvent(Mon ~ Tue, DateEvent.All, time(11.c, 23.c, 0.c))

    val result: Option[ZonedDateTime] = ce.nextElapse(refInUtc)

    assertEquals(result.map(_.getZone), Some(ZoneId.of("UTC")))
    assertEquals(result, Some(refInUtc.withMinute(23)))
  }

}
