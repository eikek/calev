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
    val r0 = Value.Range(1, 12, Some(2))
    for (n <- 1 until 12; if n % 2 == 1) {
      assert(r0.contains(n))
    }
    assert(!r0.contains(4))
    assert(!r0.contains(17))

    val r1 = Value.Range(1, 17, Some(5))
    assert(!r1.contains(0))
    assert(r1.contains(1))
    assert(r1.contains(1 + 5))
    assert(r1.contains(1 + 10))
    assert(r1.contains(1 + 15))
    assert(!r1.contains(1 + 20))

    val r2 = Value.Range(1, 8, None)
    for (n <- 1 until 8) {
      assert(r2.contains(n))
    }
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
