package com.github.eikek.calev

import java.net._
import java.time._

import cats.effect._
import cats.implicits._
import fs2.Stream

case class TestDataSet(event: CalEvent, ref: ZonedDateTime, expect: List[ZonedDateTime])

object TestDataSet {
  type EA[B] = Either[Throwable, B]

  def readResource[F[_]: Sync](name: String): Stream[F, EA[TestDataSet]] =
    Option(getClass.getResource(name)) match {
      case Some(url) =>
        read[F](url)

      case None =>
        sys.error(s"Resource not found: $name")
    }

  def read[F[_]: Sync](url: URL): Stream[F, EA[TestDataSet]] =
    fs2.io
      .readInputStream(Sync[F].delay(url.openStream), 8192)
      .through(fs2.text.utf8.decode)
      .through(fs2.text.lines)
      .filter(l => !l.trim.startsWith("#"))
      .split(_.trim.isEmpty)
      .filter(_.nonEmpty)
      .map(c => fromLines(c.toList))

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
