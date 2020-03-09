package com.github.eikek.calev

import minitest._
import Dsl._
import java.time.ZonedDateTime
import java.time.LocalDate
import java.time.LocalTime

object CalEventTest extends SimpleTestSuite {

  test("contains") {
    val ce = CalEvent(Mon ~ Tue, DateEvent.All, time(0.c, 10.c ++ 20.c, !0))
    assert(ce.contains(zdt(2015, 10, 12, 0, 10, 0)))
    assert(ce.contains(zdt(2015, 10, 13, 0, 10, 0)))
    assert(!ce.contains(zdt(2015, 10, 10, 0, 10, 0)))

    val ce2 = CalEvent(Mon ~ Wed, date(All, 5 ~ 7, All), time(0 #/ 2, 10.c ++ 20.c, !0))
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
    val ce = CalEvent(Mon ~ Tue, DateEvent.All, time(0.c, 10.c ++ 20.c, !0))
    assertEquals(ce.asString, "Mon..Tue *-*-* 00:10,20:00")

    val ce2 = CalEvent(Mon ~ Wed, date(All, 5 ~ 7, All), time(0 #/ 2, 10.c ++ 20.c, !0))
    assertEquals(ce2.asString, "Mon..Wed *-05..07-* 00/2:10,20:00")

    val ce3 = CalEvent(AllWeekdays, DateEvent.All, time(0.c, 0.c, 0.c))
    assertEquals(ce3.asString, "*-*-* 00:00:00")
  }

  test("validate") {
    val ce = CalEvent(Mon ~ Tue, date(All, 25.c, All), time(0.c, 10.c ++ 20.c, !0))
    assertEquals("Value 25 not in range [1,12]", ce.validate.head)

    val ce2 = CalEvent(Mon ~ Wed, date(All, 5 ~ 7, All), time(0 #/ 2, 10.c ++ 20.c, !0))
    assert(ce2.validate.isEmpty)
  }

  test("nextElapse") {
    val ref = zdt(2020, 3, 6, 1, 47, 12).withZoneSameLocal(CalEvent.UTC)
    val ce = CalEvent(Mon.c, DateEvent.All, time(All, 10.c ++ 20.c ++ 50.c, !0))
    val expect = zdt(2020, 3, 9, 0, 10, 0)
    assertEquals(ce.nextElapse(ref), Some(expect))
  }

  test("nextElapse") {
    val ref = zdt(2020, 3, 8, 1, 47, 12).withZoneSameLocal(CalEvent.UTC)
    val ce = CalEvent(
      AllWeekdays,
      date(2017 #/ 2, 4.c, 11.c),
      time(All, 10.c ++ 20.c ++ 50.c, !0)
    )
    assertEquals(ce.nextElapse(ref), Some(zdt(2021, 4, 11, 0, 10, 0)))
  }

  test("nextElapse (past)") {
    val ref = zdt(2020, 3, 6, 1, 47, 12).withZoneSameLocal(CalEvent.UTC)
    val ce =
      CalEvent(AllWeekdays, date(2018.c, 11.c, 11.c), time(All, 10.c ++ 20.c ++ 50.c, !0))
    assertEquals(ce.nextElapse(ref), None)
  }

  test("nextElapse (past)") {
    val ref = zdt(2020, 3, 8, 1, 47, 12).withZoneSameLocal(CalEvent.UTC)
    val ce =
      CalEvent(AllWeekdays, date(2019.c, 4.c, 11.c), time(All, 10.c ++ 20.c ++ 50.c, !0))
    assertEquals(ce.nextElapse(ref), None)
  }

  test("nextElapses") {
    val ref = zdt(2020, 3, 6, 1, 47, 12).withZoneSameLocal(CalEvent.UTC)
    val ce = CalEvent(Mon.c, DateEvent.All, time(All, 10.c ++ 20.c ++ 50.c, !0))
    val expect = List(
      zdt(2020, 3, 9, 0, 10, 0),
      zdt(2020, 3, 9, 0, 20, 0),
      zdt(2020, 3, 9, 0, 50, 0),
      zdt(2020, 3, 9, 1, 10, 0),
      zdt(2020, 3, 9, 1, 20, 0)
    )
    assertEquals(ce.nextElapses(ref, 5), expect)
  }

  test("nextElapses") {
    val ref = zdt(2020, 3, 8, 1, 47, 12).withZoneSameLocal(CalEvent.UTC)
    val ce =
      CalEvent(AllWeekdays, date(2021.c, 4.c, 11.c), time(5.c, 10.c ++ 20.c ++ 50.c, !0))
    val expect =
      List(
        zdt(2021, 4, 11, 5, 10, 0),
        zdt(2021, 4, 11, 5, 20, 0),
        zdt(2021, 4, 11, 5, 50, 0)
      )
    assertEquals(ce.nextElapses(ref, 5), expect)

  }

  private def zdt(y: Int, month: Int, d: Int, h: Int, min: Int, sec: Int): ZonedDateTime =
    ZonedDateTime.of(LocalDate.of(y, month, d), LocalTime.of(h, min, sec), CalEvent.UTC)
}
