package com.github.eikek.calev

import java.time._
import java.net._
import cats.effect._
import cats.implicits._
import fs2.Stream

case class TestDataSet(event: CalEvent, ref: ZonedDateTime, expect: List[ZonedDateTime])

object TestDataSet {
  type EA[B] = Either[Throwable, B]

  def readResource[F[_]: Sync: ContextShift](
      name: String,
      blocker: Blocker
  ): Stream[F, TestDataSet] =
    Option(getClass.getResource(name)) match {
      case Some(url) =>
        read[F](url, blocker)

      case None =>
        sys.error(s"Resource not found: $name")
    }

  def read[F[_]: Sync: ContextShift](url: URL, blocker: Blocker): Stream[F, TestDataSet] =
    fs2.io
      .readInputStream(Sync[F].delay(url.openStream), 8192, blocker)
      .through(fs2.text.utf8Decode)
      .through(fs2.text.lines)
      .filter(l => !l.trim.startsWith("#"))
      .split(_.trim.isEmpty)
      .filter(_.nonEmpty)
      .map(c => fromLines(c.toList))
      .rethrow

  def fromLines(lines: List[String]): Either[Throwable, TestDataSet] =
    lines match {
      case ev :: ref :: rest =>
        for {
          event <- CalEvent.parse(ev).leftMap(new Exception(_))
          refDate <- readDateTime(ref)
          result <- rest.traverse[EA, ZonedDateTime](readDateTime)
        } yield TestDataSet(event, refDate, result)

      case _ =>
        Left(new Exception(s"Invalid dataset: $lines"))
    }

  private def readDateTime(s: String): Either[Throwable, ZonedDateTime] =
    Either.catchNonFatal(ZonedDateTime.parse(s))
}
