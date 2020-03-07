package com.github.eikek.calev

import scala.math.Ordering.Implicits._

final case class WeekdayRange(start: Weekday, end: Weekday) {

  def contains(wd: Weekday): Boolean =
    start <= wd && wd <= end

  def asString: String =
    s"${start.shortName}..${end.shortName}"

  def validate: List[String] =
    if (start < end) Nil
    else List(s"Weekday range invalid: $start >= $end")
}

object WeekdayRange {

  implicit class WeekdayRangeIter(r: WeekdayRange) {
    def foreach(f: Weekday => Unit): Unit =
      Weekday.all.withFilter(r.contains).foreach(f)

    def filter(p: Weekday => Boolean): List[Weekday] =
      Weekday.all.filter(p)
  }
}
