package com.github.eikek.calev.internal

import com.github.eikek.calev._
import Parser._

object CalEventParser {

  def time: P[TimeEvent] =
    ((comp2 <~ colon) ~ comp2 ~ ((colon ~> comp2).opt)).map { case ((h, m), s) =>
      TimeEvent(h, m, s.getOrElse(Component(0)))
    }

  def date: P[DateEvent] =
    ((comp4 <~ dash) ~ (comp2 <~ dash) ~ comp2).map { case ((y, m), d) =>
      DateEvent(y, m, d)
    }

  val weekdays: P[WeekdayComponent] =
    weekdayComponentList.opt.map {
      case Some(c) => c
      case None    => WeekdayComponent.All
    }

  def calevent: P[CalEvent] =
    ((weekdays <~ space).opt ~ (date <~ space) ~ time ~ (space ~> zoneId).opt <~ atEnd)
      .map { case (((wd, dt), tt), z) =>
        CalEvent(wd.getOrElse(WeekdayComponent.All), dt, tt, z)
      }
      .emap { ce =>
        val errs = ce.validate
        if (errs.isEmpty) Right(ce)
        else Left(errs.mkString(", "))
      }
}
