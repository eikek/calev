package com.github.eikek.calev.internal

import minitest.SimpleTestSuite
import com.github.eikek.calev._
import com.github.eikek.calev.Dsl._

object SuccessorTest extends SimpleTestSuite {

  test("expandValue") {
    val v0 = Successor.expandValue(59)(Value.Single(13, Some(4)))
    assertEquals(v0, Vector(13, 17, 21, 25, 29, 33, 37, 41, 45, 49, 53, 57))

    val v1 = Successor.expandValue(10)(Value.Single(2, None))
    assertEquals(v1, Vector(2))

//    println(Successor.expandValues(Seq(Value.Range(1, 6, Some(5))), 59))
    //systemd: *-1..6/5-0
    //  trigger: 23:01:00 23:06:00  00:01:00 … why?
  }

  test("expandValues") {
    val v0 = Successor.expandValues(Seq(Value.Single(4, Some(2)), Value.Single(8, Some(2))), 12)
    assertEquals(v0, Vector(4, 6, 8, 10, 12))
  }

  test("succN") {
    assertEquals(Successor.succN(0, 5)(2), Successor(3, false))
    assertEquals(Successor.succN(0, 5)(5), Successor(0, true))
  }

  test("succ") {
    val comp = 10.c ++ 20.c ++ 30.c
    val r0 = Successor.succ(comp, 15, 0, 50)
    assertEquals(r0, Successor(20, false))
  }
}
