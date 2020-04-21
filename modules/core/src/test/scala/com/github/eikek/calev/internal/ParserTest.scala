package com.github.eikek.calev.internal

import minitest._
import com.github.eikek.calev._
import com.github.eikek.calev.Dsl._

object ParserTest extends SimpleTestSuite {

  test("const") {
    val p = Parser.const("ab")
    assertEquals(p.run("abc"), Right(("c", "ab")))
    assertEquals(p.run("xabc"), Left("Expected ab, but got xabc"))
  }

  test("iconst") {
    val p = Parser.iconst("ab")
    assertEquals(p.run("ABC"), Right("C" -> "AB"))
  }

  test("chars") {
    val p = Parser.chars('0' to '9').map(_.toString)
    assertEquals(p.run("1abc"), Right(("abc", "1")))
  }

  test("num2") {
    val p = Parser.num2
    assertEquals(p.run("01"), Right("", 1))
    assertEquals(p.run("1"), Right("", 1))
    assertEquals(p.run("01a"), Right("a", 1))
    assertEquals(p.run("1a"), Right("a", 1))
    assertEquals(p.run("561a"), Right("1a", 56))
    assert(p.run("abcd").isLeft)
  }

  test("num4") {
    val p = Parser.num4
    assertEquals(p.run("2002"), Right("", 2002))
    assertEquals(p.run("2022abc"), Right("abc", 2022))
    assert(p.run("120").isLeft)
    assert(p.run("120ab").isLeft)
  }

  test("values") {
    val p = Parser.value(Parser.num2)
    val ps = Parser.singleValue(Parser.num2)
    assertEquals(ps.run("23"), Right("" -> Value(23)))
    assertEquals(p.run("23"), Right("" -> Value(23)))
    assertEquals(ps.run("23/3"), Right("" -> Value.Single(23, Some(3))))
    assertEquals(p.run("23/3"), Right("" -> Value.Single(23, Some(3))))

    val pr = Parser.rangeValue(Parser.num2)
    assertEquals(pr.run("2..5"), Right("" -> Value.range(2, 5)))
    assertEquals(p.run("2..5"), Right("" -> Value.range(2, 5)))
    assertEquals(pr.run("2..05"), Right("" -> Value.range(2, 5)))
    assertEquals(p.run("2..05"), Right("" -> Value.range(2, 5)))
    assertEquals(pr.run("02..5"), Right("" -> Value.range(2, 5)))
    assertEquals(pr.run("02..5/8"), Right("" -> Value.Range(2, 5, Some(8))))
    assert(pr.run("5.8").isLeft)
  }

  test("rep") {
    val p = Parser.rep(Parser.digitChar)
    assertEquals(p.run("012a"), Right("a" -> Vector('0', '1', '2')))
  }

  test("repsep") {
    val p = Parser.repsep(Parser.num2, Parser.const(",").drain)
    assertEquals(p.run("1,2,3:abc"), Right(":abc" -> Vector(1, 2, 3)))
  }

  test("component") {
    val p = Parser.comp2

    assertEquals(p.run("1,2,3"), Right(("", 1.c ++ 2.c ++ 3.c)))
    assertEquals(p.run("1,2..5,3"), Right(("", 1.c ++ (2 ~ 5) ++ 3.c)))
    assertEquals(p.run("1/5,2..5,3"), Right(("", (1 #/ 5) ++ (2 ~ 5) ++ 3.c)))
  }

  test("weekday") {
    val p = CalEventParser.weekdays
    assertEquals(p.run("Wed"), Right("" -> Wed.c))
    assertEquals(p.run("Wed,Thu"), Right("" -> (Wed.c ++ Thu.c)))
    assertEquals(p.run("Wed,Thu,Sat"), Right("" -> (Wed.c ++ Thu.c ++ Sat.c)))
    assertEquals(p.run("Wed,Thu..Fri,Sat"), Right("" -> (Wed.c ++ (Thu ~ Fri) ++ Sat.c)))
    assertEquals(p.run("*-*-*"), Right("*-*-*" -> AllWeekdays))
  }

  test("datevent") {
    val p = CalEventParser.date
    assertEquals(p.run("*-*-*"), Right("" -> DateEvent.All))
    assertEquals(p.run("*-11-*"), Right("" -> date(All, 11.c, All)))
    assertEquals(p.run("2002-*-12"), Right("" -> date(2002.c, All, 12.c)))
  }

  test("timeevent") {
    val p = CalEventParser.time
    assertEquals(p.run("*:*:*"), Right("" -> TimeEvent.All))
    assertEquals(p.run("12:*:*"), Right("" -> time(12.c, All, All)))
  }

  test("valid calevents") {
    val p = CalEventParser.calevent
    assertEquals(
      p.run("*-*-* 12:00:00"),
      Right("" -> CalEvent(AllWeekdays, DateEvent.All, time(12.c, 0.c, 0.c)))
    )
    assertEquals(
      p.run("Mon *-*-* 6,18:0:0"),
      Right("" -> CalEvent(Mon.c, DateEvent.All, time(6.c ++ 18.c, 0.c, 0.c)))
    )
    assert(p.run("Mon *-*-* 6,88:0:0").isLeft)

    assertEquals(
      p.run("*-*-* 0..5:0/1:0"),
      Right("" -> CalEvent(AllWeekdays, DateEvent.All, time(0 ~ 5, 0 #/ 1, 0.c)))
    )

    assertEquals(
      p.run("*-*-* 0..5:0/1"),
      Right("" -> CalEvent(AllWeekdays, DateEvent.All, time(0 ~ 5, 0 #/ 1, 0.c)))
    )
  }

  test("invalid calevents") {
    val p = CalEventParser.calevent
    assert(p.run("*-a-* 6:0").isLeft, "An 'a' for month")
    assert(p.run("*-*-8..2 0:0").isLeft, "Range end<start")
    assert(p.run("*-*-* 6:5a").isLeft, "Input not exhausted")
    assert(p.run("*-*-* 1a6:05").isLeft, "Letter in between numbers")
    assert(p.run("hello world").isLeft, "hello world")
    assert(p.run("*-*-*-* 0:0").isLeft, "too many date parts")
    assert(p.run("*-*-* 0:0:x").isLeft, "x in seconds")
  }
}
