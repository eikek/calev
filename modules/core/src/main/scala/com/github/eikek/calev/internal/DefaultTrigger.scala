package com.github.eikek.calev.internal

import java.time._
import java.util.{Calendar, GregorianCalendar}

import com.github.eikek.calev._

/** Generate the next date-time that matches a calendar event and is closest but after a
  * give reference date-time.
  *
  * This is done as follows: The reference date is divided into its components, like day,
  * month, year, hour, minute and second. For each component, starting from the least
  * significant one, a value matching the corresponding calendar event component is
  * chosen. For example, if the reference minute is 16 and the minute component is 0/10,
  * the closest possible value is 20.
  *
  * There are three possible outcomes for each decision (stop after first result):
  *
  *   1. the current value matches as is 2. there is one greater than the current value 3.
  *      use the first possible value (which is lower than current)
  *
  * If the result is 1) go to the next component. If the result is 2) the date has moved
  * into the future. Then all previous results must be set to the first possible value, in
  * order to get the closest date. If the result is 3) the next component may only check
  * for 2) and 3). If the result 3) carries through to the year component, the year must
  * be set to the next value. If that is not possible, there is no date-time and a `None'
  * is returned.
  */
object DefaultTrigger extends Trigger {

  def next(ref: ZonedDateTime, ev: CalEvent): Option[ZonedDateTime] = {
    val (refDate, zone) = {
      val date = ev.zone
        .map(z => ref.withZoneSameInstant(z))
        .getOrElse(ref)
      (DateTime(date.toLocalDateTime), date.getZone)
    }

    @annotation.tailrec
    def go(c: Calc): Option[ZonedDateTime] =
      run(c) match {
        case Some(dt) =>
          val zd = dt.toLocalDateTime.atZone(zone)
          // need to match weekdays in the zone of the calendar-event
          val containsDow = ev.weekday.contains(Weekday.from(zd.getDayOfWeek))
          if (containsDow)
            Some(zd.withZoneSameInstant(ref.getZone))
          else go(Calc.init(dt, ev, ref))
        case None =>
          None
      }

    go(Calc.init(refDate, ev, ref))
  }

  @annotation.tailrec
  private def run(calc: Calc): Option[DateTime] =
    calc.pos match {
      case DateTime.Pos.Year =>
        calc.flag match {
          case Flag.Exact =>
            val (ref, comp) = calc.components
            if (comp.contains(ref)) Some(calc.date)
            else None
          case Flag.Next =>
            val (ref, comp) = calc.components
            if (comp.contains(ref)) Some(calc.date)
            else
              Some(calc.date.incYear)
                .filter(dt => calc.ce.date.year.contains(dt.date.year))
          case Flag.First =>
            Some(calc.date.incYear)
              .filter(dt => calc.ce.date.year.contains(dt.date.year))
        }

      case _ =>
        val (ref: Int, comp: Component) = calc.components
        val prevFlag: Flag = calc.flag

        if (comp.contains(ref) && prevFlag != Flag.First) {
          run(calc.copy(flag = Flag.Exact).nextPos)
        } else
          comp.findFirst(min = ref + 1, max = calc.maxValue) match {
            case Some(v) =>
              run(calc.set(v, Flag.Next).atStartBelowCurrent.nextPos)

            case None =>
              if (calc.isMonth) {
                comp.findFirst(min = calc.minValue, max = calc.maxValue) match {
                  case Some(v) =>
                    run(calc.set(v, Flag.First).atStartBelowCurrent.nextPos)
                  case None =>
                    val pos = calc
                      .set(
                        comp
                          .findFirst(calc.minValue, calc.maxValue)
                          .getOrElse(calc.minValue),
                        Flag.First
                      )
                      .nextPos
                    run(pos)
                }
              } else {
                val n =
                  comp.findFirst(calc.minValue, calc.maxValue).getOrElse(calc.minValue)
                run(calc.set(n, Flag.First).nextPos)
              }

          }
    }

  case class Date(year: Int, month: Int, day: Int) {
    def toLocalDate: LocalDate = {
      val lastDayOfMonth = YearMonth.of(year, month).atEndOfMonth().getDayOfMonth
      LocalDate.of(year, month, math.min(day, lastDayOfMonth))
    }

    def incYear: Date =
      Date(year + 1, month, day)
  }
  object Date {
    def apply(ld: LocalDate): Date =
      Date(ld.getYear, ld.getMonth.getValue, ld.getDayOfMonth)
  }
  case class Time(hour: Int, minute: Int, second: Int) {
    def toLocalTime: LocalTime =
      LocalTime.of(hour, minute, second)
  }
  object Time {
    def apply(lt: LocalTime): Time =
      Time(lt.getHour, lt.getMinute, lt.getSecond)
  }

  case class DateTime(date: Date, time: Time) {
    def toLocalDateTime: LocalDateTime =
      LocalDateTime.of(date.toLocalDate, time.toLocalTime)

    def toZonedDateTime(zone: ZoneId): ZonedDateTime =
      toLocalDateTime.atZone(zone)

    def incYear: DateTime =
      DateTime(date.incYear, time)

    def incSecond: DateTime =
      DateTime(toLocalDateTime.plusSeconds(1))
  }
  object DateTime {
    def apply(dt: LocalDateTime): DateTime =
      DateTime(Date(dt.toLocalDate), Time(dt.toLocalTime))

    sealed trait Pos {
      def next: Pos
      def prev: Pos
    }
    object Pos {
      val min = Sec
      val max = Year

      case object Sec extends Pos {
        val next = Min
        val prev = Year
      }
      case object Min extends Pos {
        val next = Hour
        val prev = Sec
      }
      case object Hour extends Pos {
        val next = Day
        val prev = Min
      }
      case object Day extends Pos {
        val next = Month
        val prev = Hour
      }
      case object Month extends Pos {
        val next = Year
        val prev = Day
      }
      case object Year extends Pos {
        val next = Sec
        val prev = Month
      }
    }
  }

  sealed trait Flag
  object Flag {
    case object Exact extends Flag
    case object Next extends Flag
    case object First extends Flag
  }

  case class Calc(flag: Flag, date: DateTime, pos: DateTime.Pos, ce: CalEvent) {
    def components: (Int, Component) =
      pos match {
        case DateTime.Pos.Sec =>
          (date.time.second, ce.time.seconds)
        case DateTime.Pos.Min =>
          (date.time.minute, ce.time.minute)
        case DateTime.Pos.Hour =>
          (date.time.hour, ce.time.hour)
        case DateTime.Pos.Day =>
          (date.date.day, ce.date.day)
        case DateTime.Pos.Month =>
          (date.date.month, ce.date.month)
        case DateTime.Pos.Year =>
          (date.date.year, ce.date.year)
      }

    def maxValue: Int =
      pos match {
        case DateTime.Pos.Sec  => 59
        case DateTime.Pos.Min  => 59
        case DateTime.Pos.Hour => 23
        case DateTime.Pos.Day  =>
          val isLeap = date.date.toLocalDate.isLeapYear
          Month.of(date.date.month).length(isLeap)
        case DateTime.Pos.Month => 12
        case DateTime.Pos.Year  => Int.MaxValue
      }

    def minValue: Int =
      pos match {
        case DateTime.Pos.Day   => 1
        case DateTime.Pos.Month => 1
        case DateTime.Pos.Year  => Int.MaxValue
        case _                  => 0
      }

    def nextPos: Calc =
      copy(pos = pos.next)

    private def atStartBelow(pos: DateTime.Pos): Calc =
      pos match {
        case DateTime.Pos.Sec =>
          this
        case _ =>
          val prev = copy(pos = pos.prev)
          val min = prev.components._2
            .findFirst(prev.minValue, prev.maxValue)
            .getOrElse(prev.minValue)
          atStartBelow(pos.prev).set(min, pos.prev)
      }

    def atStartBelowCurrent: Calc =
      atStartBelow(pos)

    def set(value: Int, flag: Flag): Calc =
      set(value, pos).copy(flag = flag)

    def set(value: Int, pos: DateTime.Pos): Calc =
      pos match {
        case DateTime.Pos.Sec =>
          copy(date = date.copy(time = date.time.copy(second = value)))
        case DateTime.Pos.Min =>
          copy(date = date.copy(time = date.time.copy(minute = value)))
        case DateTime.Pos.Hour =>
          copy(date = date.copy(time = date.time.copy(hour = value)))
        case DateTime.Pos.Day =>
          copy(date = date.copy(date = date.date.copy(day = value)))
        case DateTime.Pos.Month =>
          copy(date = date.copy(date = date.date.copy(month = value)))
        case DateTime.Pos.Year =>
          copy(date = date.copy(date = date.date.copy(year = value)))
      }

    def isMonth: Boolean =
      pos == DateTime.Pos.Month
  }

  object Calc {

    private val HOUR_MILLIS = 3600000

    def init(dt: DateTime, ce: CalEvent, ref: ZonedDateTime): Calc = {
      val zd = dt.toZonedDateTime(ce.zone.getOrElse(CalEvent.UTC))
      val dstOffsetDiff = getDstOffsetHours(zd) - getDstOffsetHours(ref)
      // at DST start hour and ZONE_OFFSET change in ZonedDateTime, at DST end only ZONE_OFFSET changes in ZonedDateTime
      val hourDstOffset = if (dstOffsetDiff > 0) dstOffsetDiff else 0
      val ndt =
        if (
          ce.copy(weekday = WeekdayComponent.All)
            .contains(zd, hourDstOffset)
        )
          dt.incSecond
        else dt
      Calc(Flag.Exact, ndt, DateTime.Pos.Sec, ce)
    }

    private def getDstOffsetHours(zd: ZonedDateTime) =
      GregorianCalendar.from(zd).get(Calendar.DST_OFFSET) / HOUR_MILLIS
  }

}
