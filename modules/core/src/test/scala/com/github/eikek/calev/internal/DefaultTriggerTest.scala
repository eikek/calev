package com.github.eikek.calev.internal

import minitest._
import DefaultTrigger._
import java.time.LocalDateTime
import com.github.eikek.calev.CalEvent

object DefaultTriggerTest extends SimpleTestSuite {

  val ce = CalEvent.unsafe("*-*-* 0/2:0")

  test("Calc.atStartBelow") {
    val dt = DateTime(LocalDateTime.now)
    val c = Calc(Flag.Exact, dt, DateTime.Pos.Hour, ce)

    val next = c.atStartBelowCurrent
    assertEquals(next.date.time.second, 0)
    assertEquals(next.date.time.minute, 0)
    assertEquals(next.date.time.hour, dt.time.hour)
    assertEquals(next.date.date, dt.date)
  }

  test("Calc.atStartBelow 2") {
    val dt = DateTime(LocalDateTime.now)
    val c = Calc(Flag.Exact, dt, DateTime.Pos.Month, ce)

    val next = c.atStartBelowCurrent
    assertEquals(next.date.time.second, 0)
    assertEquals(next.date.time.minute, 0)
    assertEquals(next.date.time.hour, 0)
    assertEquals(next.date.date.day, 1)
    assertEquals(next.date.date.month, dt.date.month)
    assertEquals(next.date.date.year, dt.date.year)
  }

  test("Calc.atStartBelow 3") {
    val ce = CalEvent.unsafe("*-*-* 0/2:10,20,50:0")
    val dt = DateTime(Date(2020, 2, 15), Time(12, 13, 14))
    val c = Calc(Flag.Exact, dt, DateTime.Pos.Hour, ce)

    val next = c.atStartBelowCurrent
    assertEquals(next.date.time.second, 0)
    assertEquals(next.date.time.minute, 10)
    assertEquals(next.date.time.hour, dt.time.hour)
    assertEquals(next.date.date, dt.date)
  }

  test("Calc.maxValue") {
    val dt = DateTime(Date(2020, 2, 15), Time(12, 0, 0))
    val c = Calc(Flag.Exact, dt, DateTime.Pos.Day, ce)

    assertEquals(c.maxValue, 29)
    assertEquals(c.nextPos.maxValue, 12)
  }
}
