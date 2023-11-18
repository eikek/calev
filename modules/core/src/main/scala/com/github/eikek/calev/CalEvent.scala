package com.github.eikek.calev

import java.time._

import scala.collection.immutable.Seq

final case class CalEvent(
    weekday: WeekdayComponent,
    date: DateEvent,
    time: TimeEvent,
    zone: Option[ZoneId] = None
) {

  def contains(ts: Instant): Boolean =
    contains(ts.atZone(zone.getOrElse(CalEvent.UTC)))

  def contains(zdate: ZonedDateTime): Boolean = {
    val zd = zone.map(z => zdate.withZoneSameInstant(z)).getOrElse(zdate)
    weekday.contains(Weekday.from(zd.getDayOfWeek)) &&
    date.year.contains(zd.getYear()) &&
    date.month.contains(zd.getMonth().getValue()) &&
    date.day.contains(zd.getDayOfMonth()) &&
    time.hour.contains(zd.getHour()) &&
    time.minute.contains(zd.getMinute()) &&
    time.seconds.contains(zd.getSecond())
  }

  def asString: String =
    (s"${weekday.asString} " +
      s"${date.year.asString}-${date.month.asString}-${date.day.asString} " +
      s"${time.hour.asString}:${time.minute.asString}:${time.seconds.asString} " +
      s"${zone.map(_.getId()).getOrElse("")}").trim

  def validate: Seq[String] =
    weekday.validate ++
      date.year.validate(0, Int.MaxValue) ++
      date.month.validate(1, 12) ++
      date.day.validate(1, 31) ++
      time.hour.validate(0, 23) ++
      time.minute.validate(0, 59) ++
      time.seconds.validate(0, 59)

  def nextElapse(ref: ZonedDateTime): Option[ZonedDateTime] =
    internal.DefaultTrigger.next(ref, this)

  def nextElapse(ref: LocalDateTime): Option[LocalDateTime] =
    nextElapse(ref.atZone(CalEvent.UTC)).map(_.toLocalDateTime)

  def nextElapses(ref: ZonedDateTime, count: Int): List[ZonedDateTime] =
    internal.DefaultTrigger.nextRepeat(count)(ref, this)

  def nextElapses(ref: LocalDateTime, count: Int): List[LocalDateTime] =
    nextElapses(ref.atZone(CalEvent.UTC), count).map(_.toLocalDateTime)
}

object CalEvent {

  val UTC: ZoneId = ZoneOffset.UTC

  def parse(str: String): Either[String, CalEvent] =
    internal.CalEventParser.calevent.run(str.trim).map(_._2)

  def unsafe(str: String): CalEvent =
    parse(str).fold(sys.error, identity)
}
