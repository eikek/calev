package com.github.eikek.calev

import java.time._
import java.time.temporal.ChronoField

import com.github.eikek.calev.Dsl._
import munit._

class CalEventTest extends FunSuite {

  test("contains") {
    val ce = CalEvent(Mon ~ Tue, DateEvent.All, time(0.c, 10.c ++ 20.c, 0.c))
    assert(ce.contains(zdt(2015, 10, 12, 0, 10, 0)))
    assert(ce.contains(zdt(2015, 10, 13, 0, 10, 0)))
    assert(!ce.contains(zdt(2015, 10, 10, 0, 10, 0)))

    val ce2 = CalEvent(Mon ~ Wed, date(All, 5 ~ 7, All), time(0 #/ 2, 10.c ++ 20.c, 0.c))
    assert(!ce2.contains(zdt(2015, 10, 12, 0, 10, 0)))
    assert(!ce2.contains(zdt(2015, 10, 13, 0, 10, 0)))
    assert(!ce2.contains(zdt(2015, 10, 10, 0, 10, 0)))
    assert(ce2.contains(zdt(2015, 6, 17, 0, 10, 0)))
    assert(ce2.contains(zdt(2015, 6, 17, 2, 10, 0)))
    assert(ce2.contains(zdt(2015, 6, 17, 4, 10, 0)))
    assert(ce2.contains(zdt(2015, 6, 17, 6, 10, 0)))
    assert(!ce2.contains(zdt(2015, 6, 17, 7, 10, 0)))
  }

  test("asString") {
    val ce = CalEvent(Mon ~ Tue, DateEvent.All, time(0.c, 10.c ++ 20.c, 0.c))
    assertEquals(ce.asString, "Mon..Tue *-*-* 00:10,20:00")

    val ce2 = CalEvent(Mon ~ Wed, date(All, 5 ~ 7, All), time(0 #/ 2, 10.c ++ 20.c, 0.c))
    assertEquals(ce2.asString, "Mon..Wed *-05..07-* 00/2:10,20:00")

    val ce3 = CalEvent(AllWeekdays, DateEvent.All, time(0.c, 0.c, 0.c))
    assertEquals(ce3.asString, "*-*-* 00:00:00")
  }

  test("validate") {
    val ce = CalEvent(Mon ~ Tue, date(All, 25.c, All), time(0.c, 10.c ++ 20.c, 0.c))
    assertEquals("Value 25 not in range [1,12]", ce.validate.head)

    val ce2 = CalEvent(Mon ~ Wed, date(All, 5 ~ 7, All), time(0 #/ 2, 10.c ++ 20.c, 0.c))
    assert(ce2.validate.isEmpty)
  }

  test("with timezone") {
    val ce = CalEvent(Mon ~ Wed, date(All, All, All), time(15.c, 0.c, 0.c))
      .copy(zone = Some(ZoneId.of("UTC")))
    assertEquals(ce.asString, "Mon..Wed *-*-* 15:00:00 UTC")

    val ce2 = CalEvent(Mon ~ Wed, date(All, All, All), time(15.c, 0.c, 0.c))
      .copy(zone = Some(ZoneOffset.UTC))
    assertEquals(ce2.asString, "Mon..Wed *-*-* 15:00:00 Z")

    val ce3 = CalEvent.unsafe("Mon..Wed *-*-* 15:00:00 UTC")
    assertEquals(ce3, ce)

    val ce4 = CalEvent.unsafe("Mon..Wed *-*-* 15:00:00 Z")
    assertEquals(ce4, ce2)
  }

  test("nextElapse no millis") {
    val ce = CalEvent.unsafe("*-*-* 0/2:0/10")
    val ref = zdt(2020, 4, 2, 18, 31, 12).`with`(ChronoField.MILLI_OF_SECOND, 156)
    assertEquals(ref.getNano, 156000000)
    val next = ce.nextElapse(ref).get
    assertEquals(next.getNano, 0)
  }

  test("nextElapse respects DST with ref before time change - DST start") {
    val ce = CalEvent.unsafe("Mon *-*-* 02:00:00 Europe/Warsaw")
    val ref = zdtInZone(2025, 3, 27, 2, 0, 0, ZoneId.of("Europe/Warsaw"))
    val next = ce.nextElapse(ref).get
    assertEquals(next.getZone.getId, "Europe/Warsaw")
    assertEquals(next.toLocalDate, LocalDate.of(2025, 3, 31))
    assertEquals(next.toLocalTime, LocalTime.of(2, 0, 0))
  }

  test("nextElapse respects DST with ref before time change - in DST") {
    val ce = CalEvent.unsafe("Mon *-*-* 02:00:00 Europe/Warsaw")
    val ref = zdtInZone(2025, 4, 27, 2, 0, 0, ZoneId.of("Europe/Warsaw"))
    val next = ce.nextElapse(ref).get
    assertEquals(next.getZone.getId, "Europe/Warsaw")
    assertEquals(next.toLocalDate, LocalDate.of(2025, 4, 28))
    assertEquals(next.toLocalTime, LocalTime.of(2, 0, 0))
  }

  test("nextElapse respects DST with ref before time change - DST end") {
    val ce = CalEvent.unsafe("Mon *-*-* 03:00:00 Europe/Warsaw")
    val ref = zdtInZone(2025, 10, 24, 3, 0, 0, ZoneId.of("Europe/Warsaw"))
    val next = ce.nextElapse(ref).get
    assertEquals(next.getZone.getId, "Europe/Warsaw")
    assertEquals(next.toLocalDate, LocalDate.of(2025, 10, 27))
    assertEquals(next.toLocalTime, LocalTime.of(3, 0, 0))
  }

  test("nextElapse respects DST with ref at time change - DST start") {
    val ce = CalEvent.unsafe("Sun *-*-* 02:00:00 Europe/Warsaw")
    val ref = zdtInZone(2025, 3, 30, 2, 0, 0, ZoneId.of("Europe/Warsaw"))
    val next = ce.nextElapse(ref).get
    assertEquals(next.getZone.getId, "Europe/Warsaw")
    assertEquals(next.toLocalDate, LocalDate.of(2025, 4, 6))
    assertEquals(next.toLocalTime, LocalTime.of(2, 0, 0))
  }

  test("nextElapse respects DST with ref at time change - DST end") {
    val ce = CalEvent.unsafe("Sun *-*-* 03:00:00 Europe/Warsaw")
    val ref = zdtInZone(2025, 10, 26, 3, 0, 0, ZoneId.of("Europe/Warsaw"))
    val next = ce.nextElapse(ref).get
    assertEquals(next.getZone.getId, "Europe/Warsaw")
    assertEquals(next.toLocalDate, LocalDate.of(2025, 11, 2))
    assertEquals(next.toLocalTime, LocalTime.of(3, 0, 0))
  }

  test(
    "nextElapse respects DST with ref before time change and CalEvent exactly at time change - DST start"
  ) {
    val ce = CalEvent.unsafe("Sun *-*-* 02:00:00 Europe/Warsaw")
    val ref = zdtInZone(2025, 3, 27, 2, 0, 0, ZoneId.of("Europe/Warsaw"))
    val next = ce.nextElapse(ref).get
    assertEquals(next.getZone.getId, "Europe/Warsaw")
    assertEquals(next.toLocalDate, LocalDate.of(2025, 3, 30))
    assertEquals(next.toLocalTime, LocalTime.of(3, 0, 0))
  }

  test(
    "nextElapse respects DST with ref before time change and CalEvent exactly at time change - DST end"
  ) {
    val ce = CalEvent.unsafe("Sun *-*-* 03:00:00 Europe/Warsaw")
    val ref = zdtInZone(2025, 10, 23, 3, 0, 0, ZoneId.of("Europe/Warsaw"))
    val next = ce.nextElapse(ref).get
    assertEquals(next.getZone.getId, "Europe/Warsaw")
    assertEquals(next.toLocalDate, LocalDate.of(2025, 10, 26))
    assertEquals(next.toLocalTime, LocalTime.of(3, 0, 0))
  }

  test("nextElapse respects DST with ref after time change - DST start") {
    val ce = CalEvent.unsafe("Sun *-*-* 02:00:00 Europe/Warsaw")
    val ref = zdtInZone(2025, 4, 20, 2, 0, 0, ZoneId.of("Europe/Warsaw"))
    val next = ce.nextElapse(ref).get
    assertEquals(next.getZone.getId, "Europe/Warsaw")
    assertEquals(next.toLocalTime, LocalTime.of(2, 0, 0))
  }

  test("nextElapses ends") {
    val ref = zdt(2020, 3, 8, 1, 47, 12).withZoneSameLocal(CalEvent.UTC)
    val ce =
      CalEvent(AllWeekdays, date(2021.c, 4.c, 11.c), time(5.c, 10.c ++ 20.c ++ 50.c, 0.c))
    val expect =
      List(
        zdt(2021, 4, 11, 5, 10, 0),
        zdt(2021, 4, 11, 5, 20, 0),
        zdt(2021, 4, 11, 5, 50, 0)
      )
    assertEquals(ce.nextElapses(ref, 5), expect)
  }

  test("nextElapse honors time zones") {
    val ref = zdt(2022, 2, 28, 21, 5, 15)

    val ce = CalEvent(
      AllWeekdays,
      DateEvent.All,
      time(22.c, 10.c, 0.c),
      Some(ZoneId.of("Europe/Berlin"))
    )

    val next = ce.nextElapse(ref).get
    assertEquals(next.getZone, ZoneOffset.UTC)
    assertEquals(next.toLocalTime, LocalTime.of(21, 10, 0))
  }

  test("day after last January is not 31st February") {
    val expression = CalEvent(AllWeekdays, date(All, 2.c, All), time(0 #/ 6, 0.c, 0.c))

    val next = expression
      .nextElapse(OffsetDateTime.parse("2024-01-31T18:00+01:00").toZonedDateTime)
      .get

    assertEquals(next, ZonedDateTime.parse("2024-02-01T00:00+01:00"))
  }

  test(
    "expression for the daily run on some month with potential next run on next year should start at the beginning of the month next year"
  ) {
    val expression = CalEvent.unsafe("*-2-* 10:00:00")

    val next = expression.nextElapse(LocalDateTime.parse("2024-03-20T10:01:00"))
    assertEquals(
      next.get,
      LocalDateTime.parse("2025-02-01T10:00")
    )
  }

  test(
    "expression for the daily run on some month with ref date before (1 month before) aforementioned month should start at the beginning of the month of the same year"
  ) {
    val expression = CalEvent.unsafe("*-2-* 10:00:00")

    val next = expression.nextElapse(LocalDateTime.parse("2024-01-20T10:01:00"))
    assertEquals(
      next.get,
      LocalDateTime.parse("2024-02-01T10:00")
    )
  }

  test(
    "expression for the daily run on some month with ref date before (few months before) aforementioned month should start at the beginning of the month of the same year"
  ) {
    val expression = CalEvent.unsafe("*-4-* 10:00:00")

    val next = expression.nextElapse(LocalDateTime.parse("2024-01-20T10:01:00"))
    assertEquals(
      next.get,
      LocalDateTime.parse("2024-04-01T10:00")
    )
  }

  test(
    "expression for the daily run on some month with ref date before (on the same month) aforementioned month should start at the beginning of the month of the same year"
  ) {
    val expression = CalEvent.unsafe("*-4-25 10:00:00")

    val next = expression.nextElapse(LocalDateTime.parse("2024-04-20T10:01:00"))
    assertEquals(
      next.get,
      LocalDateTime.parse("2024-04-25T10:00")
    )
  }

  test(
    "expression for the monthly run on some month with potential next run on next year should start at the beginning of the month next year"
  ) {
    val expression = CalEvent.unsafe("*-4-25 10:00:00")

    val next = expression.nextElapse(LocalDateTime.parse("2024-08-20T10:01:00"))
    assertEquals(
      next.get,
      LocalDateTime.parse("2025-04-25T10:00")
    )
  }

  private def zdt(y: Int, month: Int, d: Int, h: Int, min: Int, sec: Int): ZonedDateTime =
    ZonedDateTime.of(LocalDate.of(y, month, d), LocalTime.of(h, min, sec), ZoneOffset.UTC)

  private def zdtInZone(
      y: Int,
      month: Int,
      d: Int,
      h: Int,
      min: Int,
      sec: Int,
      zone: ZoneId
  ): ZonedDateTime =
    ZonedDateTime.of(LocalDate.of(y, month, d), LocalTime.of(h, min, sec), zone)
}
