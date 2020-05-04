package com.github.eikek.fs2calev

import _root_.fs2._
import cats.implicits._
import cats.effect._
import com.github.eikek.calev._
import cats.ApplicativeError
import java.time.ZonedDateTime
import scala.concurrent.duration.FiniteDuration
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

object CalevFs2 {

  def parse[F[_]](str: String)(implicit E: ApplicativeError[F, Throwable]): F[CalEvent] =
    CalEvent
      .parse(str)
      .fold(
        err => E.raiseError(new Exception(err)),
        ce => ce.pure[F]
      )

  def parseStream[F[_]](
      str: String
  )(implicit E: ApplicativeError[F, Throwable]): Stream[F, CalEvent] =
    Stream.eval(parse[F](str))

  def nextElapses[F[_]](ref: ZonedDateTime)(ce: CalEvent): Stream[F, ZonedDateTime] =
    Stream
      .emit(ce.nextElapse(ref))
      .unNoneTerminate
      .flatMap(date => Stream.emit(date) ++ nextElapses(date)(ce))

  def evalNow[F[_]: Sync]: Stream[F, ZonedDateTime] =
    Stream.eval(Sync[F].delay(ZonedDateTime.now))

  def durationFromNow[F[_]: Sync](ce: CalEvent): Stream[F, FiniteDuration] =
    evalNow[F]
      .map(now =>
        ce.nextElapse(now)
          .map { end =>
            val millis = now.until(end, ChronoUnit.MILLIS)
            FiniteDuration(millis, TimeUnit.MILLISECONDS)
          }
      )
      .unNoneTerminate

  def sleep[F[_]: Sync](ce: CalEvent)(implicit T: Timer[F]): Stream[F, Unit] =
    durationFromNow[F](ce).flatMap(Stream.sleep[F])

  def awakeEvery[F[_]: Sync](ce: CalEvent)(implicit T: Timer[F]): Stream[F, Unit] =
    sleep(ce).repeat

  def schedule[F[_]: Concurrent, A](tasks: List[(CalEvent, Stream[F, A])])(implicit
      timer: Timer[F]
  ): Stream[F, A] = {
    val scheduled = tasks.map { case (ce, task) => awakeEvery[F](ce) >> task }
    Stream.emits(scheduled).covary[F].parJoinUnbounded
  }
}
