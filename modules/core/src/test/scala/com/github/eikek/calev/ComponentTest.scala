package com.github.eikek.calev

import minitest._
import Dsl._
import java.time.ZonedDateTime
import java.time.LocalDate
import java.time.LocalTime

object ComponentTest extends SimpleTestSuite {

  test("create components") {

    assertEquals(
      Component(12),
      Component.List(Seq(Value.Single(12, None): Value))
    )
    assertEquals(
      Component(12, 13),
      Component.List(Seq(Value.Single(12, None): Value, Value.Single(13, None): Value))
    )

  }

  test("contains") {
    assert(!2.c.contains(3))
    for (n <- 1 to 59; if n % 2 == 0) {
      assert(All.contains(n))
      assert(n.c.contains(n))
      assert(!n.c.contains(n + 1))
    }

    assert(!(2 ~ 5).contains(1))
    assert((2 ~ 5).contains(2))
    assert((2 ~ 5).contains(3))
    assert((2 ~ 5).contains(4))
    assert((2 ~ 5).contains(5))
    assert(!(2 ~ 5).contains(6))

    assert((2.c ++ 4.c ++ 6.c).contains(2))
    assert(!(2.c ++ 4.c ++ 6.c).contains(3))
    assert((2.c ++ 4.c ++ 6.c).contains(4))
    assert(!(2.c ++ 4.c ++ 6.c).contains(5))
    assert((2.c ++ 4.c ++ 6.c).contains(6))
    assert(!(2.c ++ 4.c ++ 6.c).contains(7))
  }

  test("asString") {
    assertEquals(2.c.asString, "02")
    assertEquals(10.c.asString, "10")
    assertEquals((2 ~ 5).asString, "02..05")

    assertEquals((2.c ++ 4.c ++ 6.c).asString, "02,04,06")
    assertEquals((2.c ++ 4 #/ 2 ++ 6.c).asString, "02,04/2,06")
  }

}
