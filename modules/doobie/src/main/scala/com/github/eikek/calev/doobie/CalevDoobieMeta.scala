package com.github.eikek.calev.doobie

import _root_.doobie.util.invariant._
import _root_.doobie.{Meta, Read, Write}
import cats.implicits._
import com.github.eikek.calev._
import com.github.eikek.calev.doobie.CalevDoobieMeta.parseOrThrow
import org.tpolecat.typename.TypeName

trait CalevDoobieMeta {

  implicit val caleventRead: Read[CalEvent] =
    Read[String].map(parseOrThrow(CalEvent.parse))

  implicit val caleventWrite: Write[CalEvent] =
    Write[String].contramap(_.asString)

  implicit val caleventMeta: Meta[CalEvent] =
    Meta[String].imap(parseOrThrow(CalEvent.parse))(_.asString)

}

object CalevDoobieMeta extends CalevDoobieMeta {

  private def parseOrThrow[A](
      f: String => Either[String, A]
  )(str: String)(implicit ev: TypeName[A]): A =
    f(str) match {
      case Right(a)  => a
      case Left(err) => throw InvalidValue[String, A](str, err)
    }

}
