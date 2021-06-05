package com.github.eikek.calev

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoField

import scala.concurrent.ExecutionContext

import cats.effect._
import com.github.eikek.calev.Dsl._
import munit._

class CalEventTest extends FunSuite {

  implicit val CS: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  val blocker                       = Blocker.liftExecutionContext(ExecutionContext.global)

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

  test("nextElapse no millis") {
    val ce  = CalEvent.unsafe("*-*-* 0/2:0/10")
    val ref = zdt(2020, 4, 2, 18, 31, 12).`with`(ChronoField.MILLI_OF_SECOND, 156)
    assertEquals(ref.getNano, 156000000)
    val next = ce.nextElapse(ref).get
    assertEquals(next.getNano, 0)
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

  private def zdt(y: Int, month: Int, d: Int, h: Int, min: Int, sec: Int): ZonedDateTime =
    ZonedDateTime.of(LocalDate.of(y, month, d), LocalTime.of(h, min, sec), CalEvent.UTC)
}
