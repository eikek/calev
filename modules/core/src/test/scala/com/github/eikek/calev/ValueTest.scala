package com.github.eikek.calev

import minitest._

object ValueTest extends SimpleTestSuite {

  test("contains (single values)") {
    for (n <- 1 to 50) {
      assert(Value(n).contains(n))
      assert(!Value(n).contains(n + 1))
      assert(!Value(n).contains(n - 1))
    }
    assert(Value.Single(2, Some(4)).contains(2))
    assert(!Value.Single(2, Some(4)).contains(3))
    assert(!Value.Single(2, Some(4)).contains(4))
    assert(!Value.Single(2, Some(4)).contains(5))
    assert(Value.Single(2, Some(4)).contains(6))
    assert(!Value.Single(2, Some(4)).contains(7))
    assert(!Value.Single(2, Some(4)).contains(8))
    assert(!Value.Single(2, Some(4)).contains(9))
    assert(Value.Single(2, Some(4)).contains(10))

    assert(!Value.Single(5, Some(1)).contains(2))
    assert(!Value.Single(5, Some(2)).contains(2))
    assert(!Value.Single(5, Some(3)).contains(2))
  }

  test("contains (ranges)") {
    /*
     1-3/2
     1-3 3-5 5-7
     */
    val r0 = Value.Range(1, 3, Some(2))
    assert(!r0.contains(0))
    for (n <- 1 to 50) {
      assert(r0.contains(n))
    }

    /*
     1-2/3
     1-2 4-5 7-8
     */
    val r1 = Value.Range(1, 2, Some(3))
    assert(!r1.contains(0))
    assert(r1.contains(1))
    assert(r1.contains(2))
    assert(!r1.contains(3))
    assert(r1.contains(4))
    assert(r1.contains(5))
    assert(!r1.contains(6))
    assert(r1.contains(7))
    assert(r1.contains(8))

    /*
     1-9/5
     1-9 6-14 11-19 16-24
     */
    val r2 = Value.Range(1, 9, Some(5))
    for (n <- 1 to 90) {
      assert(r2.contains(n))
    }

    /*
     1-3/9
     1-3 10-12 19-21 28-30
     */
    val r3 = Value.Range(1, 3, Some(9))
    assert(r3.contains(1))
    assert(r3.contains(2))
    assert(r3.contains(3))
    for (n <- 4 to 9) {
      assert(!r3.contains(n))
    }
    assert(r3.contains(10))
    assert(r3.contains(11))
    assert(r3.contains(12))
    for (n <- 13 to 18) {
      assert(!r3.contains(n))
    }
    assert(r3.contains(19))
    assert(r3.contains(20))
    assert(r3.contains(21))
    for (n <- 22 to 27) {
      assert(!r3.contains(n))
    }
    assert(r3.contains(28))
    assert(r3.contains(29))
    assert(r3.contains(30))
  }

  test("asString") {
    assertEquals(Value(1).asString, "01")
    assertEquals(Value(8).asString, "08")
    assertEquals(Value(10).asString, "10")

    assertEquals(Value.Single(10, Some(4)).asString, "10/4")

    assertEquals(Value.range(2, 5).asString, "02..05")
    assertEquals(Value.range(1, 15).asString, "01..15")

    assertEquals(Value.Range(1, 15, Some(4)).asString, "01..15/4")
  }
}
