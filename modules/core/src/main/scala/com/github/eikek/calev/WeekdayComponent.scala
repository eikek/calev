package com.github.eikek.calev

import scala.collection.immutable.Seq
sealed trait WeekdayComponent {

  def contains(day: Weekday): Boolean

  def ++(wc: WeekdayComponent): WeekdayComponent

  def asString: String

  def validate: Seq[String]
}

object WeekdayComponent {

  case object All extends WeekdayComponent {
    def contains(day: Weekday): Boolean = true
    def ++(wc: WeekdayComponent): WeekdayComponent = this
    def asString: String = ""
    def validate: Seq[String] = Nil
  }

  sealed trait WeekdayVal {
    def contains(day: Weekday): Boolean
    def asString: String
    def validate: Seq[String]
  }

  object WeekdayVal {
    case class Single(day: Weekday) extends WeekdayVal {
      def contains(d: Weekday): Boolean =
        d == day

      def asString: String =
        day.shortName

      def validate: Seq[String] = Nil
    }

    case class Range(range: WeekdayRange) extends WeekdayVal {
      def contains(d: Weekday): Boolean =
        range.contains(d)

      def asString: String =
        range.asString

      def validate: Seq[String] =
        range.validate
    }
  }

  case class List(values: Seq[WeekdayVal]) extends WeekdayComponent {
    def contains(day: Weekday): Boolean =
      values.exists(_.contains(day))

    def ++(wc: WeekdayComponent): WeekdayComponent =
      wc match {
        case All => All
        case List(seq) =>
          List(values ++ seq)
      }

    def asString: String =
      values.map(_.asString).mkString(",")

    def validate: Seq[String] =
      values.flatMap(_.validate)
  }
}
