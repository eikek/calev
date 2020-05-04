package com.github.eikek.calev
import java.time.DayOfWeek

sealed trait Weekday {
  def ord: Int
  def shortName: String
  def longName: String
}

object Weekday {

  def from(dow: DayOfWeek): Weekday =
    dow match {
      case DayOfWeek.MONDAY    => Mon
      case DayOfWeek.TUESDAY   => Tue
      case DayOfWeek.WEDNESDAY => Wed
      case DayOfWeek.THURSDAY  => Thu
      case DayOfWeek.FRIDAY    => Fri
      case DayOfWeek.SATURDAY  => Sat
      case DayOfWeek.SUNDAY    => Sun
    }

  case object Mon extends Weekday {
    val ord       = 1
    val shortName = "Mon"
    val longName  = "Monday"
  }

  case object Tue extends Weekday {
    val ord       = 2
    val shortName = "Tue"
    val longName  = "Tuesday"
  }

  case object Wed extends Weekday {
    val ord       = 3
    val shortName = "Wed"
    val longName  = "Wednesday"
  }

  case object Thu extends Weekday {
    val ord       = 4
    val shortName = "Thu"
    val longName  = "Thursday"
  }

  case object Fri extends Weekday {
    val ord       = 5
    val shortName = "Fri"
    val longName  = "Friday"
  }

  case object Sat extends Weekday {
    val ord       = 6
    val shortName = "Sat"
    val longName  = "Saturday"
  }

  case object Sun extends Weekday {
    val ord       = 7
    val shortName = "Sun"
    val longName  = "Sunday"
  }

  val all = List(Mon, Tue, Wed, Thu, Fri, Sat, Sun)

  implicit def ordering: Ordering[Weekday] =
    Ordering.by(_.ord)

  implicit class WeekdayRangeCtor(wd1: Weekday) {
    def to(wd2: Weekday): WeekdayRange =
      WeekdayRange(wd1, wd2)
  }
}
