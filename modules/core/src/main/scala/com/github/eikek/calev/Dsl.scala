package com.github.eikek.calev

import WeekdayComponent._

object Dsl {

  val Mon: Weekday = Weekday.Mon
  val Tue: Weekday = Weekday.Tue
  val Wed: Weekday = Weekday.Wed
  val Thu: Weekday = Weekday.Thu
  val Fri: Weekday = Weekday.Fri
  val Sat: Weekday = Weekday.Sat
  val Sun: Weekday = Weekday.Sun

  val All: Component = Component.All

  val AllWeekdays: WeekdayComponent = WeekdayComponent.All

  def date(y: Component, m: Component, d: Component): DateEvent =
    DateEvent(y, m, d)

  def time(h: Component, m: Component, s: Component): TimeEvent =
    TimeEvent(h, m, s)

  implicit final class WeekdayOps(wd: Weekday) {
    def ~(wd2: Weekday): WeekdayComponent =
      List(Seq(WeekdayVal.Range(WeekdayRange(wd, wd2))))

    def c: WeekdayComponent =
      List(Seq(WeekdayVal.Single(wd)))

    def unary_! : WeekdayComponent =
      c
  }

  implicit final class IntComponent(n: Int) {
    def #/(rep: Int): Component =
      Component.List(Seq(Value.Single(n, Some(rep).filter(_ > 0))))

    def ~(end: Int): Component =
      Component.List(Seq(Value.range(n, end)))

    def unary_! : Component = c

    def c: Component =
      Component.List(Seq(Value(n)))
  }
}
