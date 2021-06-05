package com.github.eikek.calev

import com.github.eikek.calev.Weekday._
import munit._

class WeekdayRangeTest extends FunSuite {

  test("contains") {
    assert(WeekdayRange(Mon, Wed).contains(Mon))
    assert(WeekdayRange(Mon, Wed).contains(Tue))
    assert(WeekdayRange(Mon, Wed).contains(Wed))
    assert(!WeekdayRange(Mon, Wed).contains(Thu))
    assert(!WeekdayRange(Mon, Wed).contains(Fri))
    assert(!WeekdayRange(Mon, Wed).contains(Sat))
    assert(!WeekdayRange(Mon, Wed).contains(Sun))
  }

  test("asString") {
    assertEquals(WeekdayRange(Mon, Wed).asString, "Mon..Wed")
    assertEquals(WeekdayRange(Mon, Sun).asString, "Mon..Sun")
    assertEquals(WeekdayRange(Mon, Tue).asString, "Mon..Tue")
    assertEquals(WeekdayRange(Sun, Tue).asString, "Sun..Tue")
    assertEquals(WeekdayRange(Tue, Mon).asString, "Tue..Mon")
  }

  test("validate") {
    assertEquals(WeekdayRange(Mon, Wed).validate, Nil)
    assertEquals(WeekdayRange(Mon, Sun).validate, Nil)
    assertEquals(WeekdayRange(Mon, Tue).validate, Nil)
    assertEquals(WeekdayRange(Sun, Tue).validate.size, 1)
    assertEquals(WeekdayRange(Mon, Mon).validate.size, 1)
    assertEquals(WeekdayRange(Tue, Mon).validate.size, 1)
  }
}
