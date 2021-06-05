package com.github.eikek.calev

import com.github.eikek.calev.Dsl._
import munit._

class WeekdayComponentTest extends FunSuite {

  test("contains") {
    for (wd <- Weekday.all) {
      assert(wd.c.contains(wd))
      Weekday.all.filter(_ != wd).foreach(d => assert(!wd.c.contains(d)))
      assert(AllWeekdays.contains(wd))
    }

    assert((Mon ~ Wed).contains(Mon))
    assert((Mon ~ Wed).contains(Tue))
    assert((Mon ~ Wed).contains(Wed))
    assert(!(Mon ~ Wed).contains(Thu))
    assert(!(Mon ~ Wed).contains(Fri))
    assert(!(Mon ~ Wed).contains(Sat))
    assert(!(Mon ~ Wed).contains(Sun))
  }

  test("append (++)") {
    assert((Mon.c ++ Thu.c).contains(Mon))
    assert(!(Mon.c ++ Thu.c).contains(Tue))
    assert(!(Mon.c ++ Thu.c).contains(Wed))
    assert((Mon.c ++ Thu.c).contains(Thu))
    assert(!(Mon.c ++ Thu.c).contains(Fri))
    assert(!(Mon.c ++ Thu.c).contains(Sat))
    assert(!(Mon.c ++ Thu.c).contains(Sun))

    val comp = Mon.c ++ Thu.c ++ (Sat ~ Sun)
    assert(comp.contains(Mon))
    assert(!comp.contains(Tue))
    assert(!comp.contains(Wed))
    assert(comp.contains(Thu))
    assert(!comp.contains(Fri))
    assert(comp.contains(Sat))
    assert(comp.contains(Sun))

    assertEquals(AllWeekdays ++ Mon.c, AllWeekdays)
    assertEquals(Mon.c ++ AllWeekdays, AllWeekdays)
  }

  test("asString") {
    for (wd <- Weekday.all)
      assertEquals(wd.c.asString, wd.shortName)
    assertEquals((Tue ~ Thu).asString, "Tue..Thu")
    assertEquals((Sat ~ Sun).asString, "Sat..Sun")
  }

  test("validate") {
    for (wd <- Weekday.all)
      assertEquals(wd.c.validate, Nil)
    assertEquals((Wed ~ Tue).validate.size, 1)
    assertEquals((Tue ~ Wed).validate, Nil)
  }
}
