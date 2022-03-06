package com.github.eikek.calev.fs2

import java.time._
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

import scala.concurrent.duration.FiniteDuration

import cats.effect._
import cats.implicits._
import com.github.eikek.calev.CalEvent
import fs2.Stream

trait Scheduler[F[_]] {
  def fromNowUntilNext(schedule: CalEvent): F[FiniteDuration]

  def sleepUntilNext(schedule: CalEvent): F[Unit]

  def sleep(schedule: CalEvent): Stream[F, Unit]

  def awakeEvery(schedule: CalEvent): Stream[F, Unit]
}

object Scheduler {
  def systemDefault[F[_]](implicit
      temporal: Temporal[F],
      F: Sync[F]
  ): Scheduler[F] =
    from(F.delay(ZoneId.systemDefault()))

  def utc[F[_]](implicit F: Temporal[F]): Scheduler[F] =
    from(F.pure(ZoneOffset.UTC))

  def from[F[_]](zoneId: F[ZoneId])(implicit F: Temporal[F]): Scheduler[F] =
    new Scheduler[F] {
      override def fromNowUntilNext(schedule: CalEvent): F[FiniteDuration] =
        now.flatMap { from =>
          schedule.nextElapse(from) match {
            case Some(next) =>
              val durationInMillis = from.until(next, ChronoUnit.MILLIS)
              F.pure(FiniteDuration(durationInMillis, TimeUnit.MILLISECONDS))
            case None =>
              val msg = s"Could not calculate the next date-time from $from " +
                s"given the calendar event expression '${schedule.asString}'. This should never happen."
              F.raiseError(new Throwable(msg))
          }
        }

      def sleepUntilNext(schedule: CalEvent): F[Unit] =
        Temporal[F].flatMap(fromNowUntilNext(schedule))(Temporal[F].sleep)

      def sleep(schedule: CalEvent): Stream[F, Unit] =
        Stream.eval(sleepUntilNext(schedule))

      def awakeEvery(schedule: CalEvent): Stream[F, Unit] =
        sleep(schedule).repeat

      private val now: F[ZonedDateTime] =
        (F.realTimeInstant, zoneId).mapN(_.atZone(_))
    }
}
