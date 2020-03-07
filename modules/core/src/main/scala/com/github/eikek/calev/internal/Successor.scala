package com.github.eikek.calev.internal

import com.github.eikek.calev._
import java.time.ZonedDateTime
import java.time.temporal.ChronoField
import java.time.LocalTime
import java.time.LocalDate

final case class Successor[A](n: A, carry: Boolean) {

  def map[B](f: A => B): Successor[B] =
    Successor(f(n), carry)
}

object Successor {

  def nextRepeat(ref: ZonedDateTime, ev: CalEvent, count: Int): List[ZonedDateTime] = {
    @annotation.tailrec
    def go(result: List[ZonedDateTime], cur: ZonedDateTime, i: Int): List[ZonedDateTime] =
      if (i == 0) result
      else
        next(cur, ev) match {
          case Some(zd) => go(zd :: result, zd, i - 1)
          case None     => result
        }

    go(Nil, ref, count).reverse
  }

  def next(ref: ZonedDateTime, ev: CalEvent): Option[ZonedDateTime] =
    Option(next2(ref, ev)).filter(_.isAfter(ref)).map { zd =>
      if (zd.toLocalDate.isAfter(ref.toLocalDate)) {
        val begin = nextTime(LocalTime.MIN, ev.time)
        zd.withHour(begin.getHour).withMinute(begin.getMinute).withSecond(begin.getSecond)
      } else zd
    }

  def nextTime(ref: LocalTime, ev: TimeEvent): LocalTime = {
    val mkZDT: LocalTime => ZonedDateTime =
      lt => ZonedDateTime.of(LocalDate.MIN, lt, CalEvent.UTC)
    next2(mkZDT(ref), CalEvent(WeekdayComponent.All, DateEvent.All, ev)).toLocalTime
  }

  @annotation.tailrec
  private def next2(ref: ZonedDateTime, ev: CalEvent): ZonedDateTime = {
    val zd = next0(ref, ev)
    if (ev.weekday.contains(Weekday.from(zd.getDayOfWeek)))
      zd.`with`(ChronoField.MILLI_OF_SECOND, 0)
    else next2(zd, ev)
  }

  private def next0(ref: ZonedDateTime, ev: CalEvent): ZonedDateTime = {
    val nextf = List[Successor[ZonedDateTime] => Successor[ZonedDateTime]](
      el => secondSucc(ev.time.seconds, el.n.getSecond).map(s => el.n.withSecond(s)),
      el => minuteSucc(ev.time.minute, el.n.getMinute).map(m => el.n.withMinute(m)),
      el => hourSucc(ev.time.hour, el.n.getHour).map(h => el.n.withHour(h)),
      el => daySucc(el.n)(ev.date.day).map(d => el.n.withDayOfMonth(d)),
      el => monthSucc(ev.date.month, el.n.getMonthValue).map(m => el.n.withMonth(m)),
      el => yearSucc(ev.date.year, el.n.getYear).map(y => el.n.withYear(y))
    )
    val findf = List[Successor[ZonedDateTime] => Successor[ZonedDateTime]](
      el => find(ev.time.seconds, el.n.getSecond, 0, 59).map(s => el.n.withSecond(s)),
      el => find(ev.time.minute, el.n.getMinute, 0, 59).map(m => el.n.withMinute(m)),
      el => find(ev.time.hour, el.n.getHour, 0, 23).map(h => el.n.withHour(h)),
      el => findDay(el.n)(ev.date.day).map(d => el.n.withDayOfMonth(d)),
      el => find(ev.date.month, el.n.getMonthValue, 1, 12).map(m => el.n.withMonth(m)),
      el => findYear(ev.date.year, el.n.getYear).map(y => el.n.withYear(y))
    )

    nextf
      .zip(findf)
      .foldLeft(Successor(ref, true)) {
        case (in, (sf, cf)) =>
          in match {
            case Successor(_, false) => cf(in)
            case Successor(_, true)  => sf(in)
          }
      }
      .n
  }

  def secondSucc(c: Component, value: Int): Successor[Int] =
    succ(c, value, 0, 59)

  def minuteSucc(c: Component, value: Int): Successor[Int] =
    succ(c, value, 0, 59)

  def hourSucc(c: Component, value: Int): Successor[Int] =
    succ(c, value, 0, 23)

  def daySucc(zd: ZonedDateTime)(c: Component): Successor[Int] = {
    val leap = zd.toLocalDate.isLeapYear
    val max = zd.getMonth.length(leap)
    succ(c, zd.getDayOfMonth, 1, max)
  }

  def monthSucc(c: Component, value: Int): Successor[Int] =
    succ(c, value, 1, 12)

  def yearSucc(c: Component, value: Int): Successor[Int] =
    succ(c, value, 0, value + 2)

  def succ(c: Component, value: Int, min: Int, max: Int): Successor[Int] =
    c match {
      case Component.All =>
        succN(min, max)(value)
      case Component.List(vs) =>
        val expanded = expandValues(vs, max)
        expanded
          .find(_ > value)
          .map(n => Successor(n, false))
          .getOrElse(Successor(expanded.headOption.getOrElse(min), true))
    }

  def findDay(zd: ZonedDateTime)(c: Component): Successor[Int] = {
    val leap = zd.toLocalDate.isLeapYear
    val max = zd.getMonth.length(leap)
    find(c, zd.getDayOfMonth, 1, max)
  }

  def findYear(c: Component, value: Int): Successor[Int] =
    find(c, value, 0, value + 2)

  def find(c: Component, value: Int, min: Int, max: Int): Successor[Int] =
    c match {
      case Component.All =>
        Successor(value, false)
      case Component.List(vs) =>
        val expanded = expandValues(vs, max)
        expanded
          .find(_ >= value)
          .map(n => Successor(n, false))
          .getOrElse(Successor(expanded.headOption.getOrElse(min), true))
    }

  def succN(min: Int, max: Int)(n: Int): Successor[Int] = {
    val c = n + 1
    if (c > max) Successor(min, true)
    else Successor(c, false)
  }

  def expandValues(v: Seq[Value], max: Int): Vector[Int] =
    v.flatMap(expandValue(max)).distinct.sorted.toVector

  def expandValue(max: Int)(v: Value): Vector[Int] =
    v match {
      case Value.Single(n, Some(rep)) =>
        @annotation.tailrec
        def go(v: Vector[Int], count: Int): Vector[Int] =
          if (count * rep + n > max) v
          else go(v :+ (count * rep + n), count + 1)

        go(Vector.empty, 0)

      case Value.Single(n, None) =>
        Vector(n)

      case Value.Range(start, end, Some(rep)) =>
        @annotation.tailrec
        def go(v: Vector[Int], count: Int): Vector[Int] = {
          val nextStart = count * rep + start
          if (nextStart > max) v
          else {
            val nEnd = math.min(count * rep + end, max)
            go(v ++ (nextStart to nEnd).toVector, count + 1)
          }
        }
        go(Vector.empty, 0)

      case Value.Range(start, end, None) =>
        (start to end).toVector
    }
}
