package com.github.eikek.calev.internal

import java.time.ZonedDateTime

import com.github.eikek.calev._

/** Evaluates a calendar event against a reference date to provide the next date-time
  * matching the calendar event.
  */
trait Trigger {

  def next(ref: ZonedDateTime, ev: CalEvent): Option[ZonedDateTime]

  def nextRepeat(count: Int)(ref: ZonedDateTime, ev: CalEvent): List[ZonedDateTime] = {
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

}
