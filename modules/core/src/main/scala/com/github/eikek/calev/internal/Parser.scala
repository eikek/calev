package com.github.eikek.calev.internal

import java.time.ZoneId

import scala.util.control.NonFatal

import com.github.eikek.calev._

object Parser {

  val space: P[Unit] =
    const(" ").drain

  val comma: P[Unit] =
    const(",").drain

  val colon: P[Unit] =
    const(":").drain

  val dash: P[Unit] =
    const("-").drain

  val colons: P[Unit] =
    const("..").drain

  val digitChar: P[Char] =
    chars('0' to '9')

  val num4: P[Int] =
    take(4).emap(readInt)

  val num2: P[Int] =
    (digitChar ~ digitChar.opt).emap {
      case (c0, Some(c1)) => readInt(s"$c0$c1")
      case (c0, None)     => readInt(s"$c0")
    }

  val atEnd: P[Unit] =
    P(in => if (in.isEmpty) Right(("", ())) else Left(s"Input left: $in"))

  val repVal: P[Option[Int]] =
    (const("/") ~> num2).opt

  def singleValue(num: P[Int]): P[Value] =
    (num ~ repVal).map { case (n, rep) =>
      Value.Single(n, rep)
    }

  def rangeValue(num: P[Int]): P[Value] =
    ((num <~ colons) ~ num ~ repVal).map { case ((start, end), rep) =>
      Value.Range(start, end, rep)
    }

  def value(num: P[Int]): P[Value] =
    rangeValue(num) | singleValue(num)

  val compAll: P[Component] =
    const("*").map(_ => Component.All)

  def compSeq(num: P[Int]): P[Component] =
    repsep(value(num), comma).map(vs => Component.List(vs.toSeq))

  def component(num: P[Int]): P[Component] =
    compAll | compSeq(num)

  val comp2: P[Component] =
    component(num2)

  val comp4: P[Component] =
    component(num4)

  val anyWeekday: P[Weekday] =
    Weekday.all.map(weekday).reduce(_ | _)

  val weekdayRange: P[WeekdayRange] =
    ((anyWeekday <~ colons) ~ anyWeekday).map { case (s, e) =>
      WeekdayRange(s, e)
    }

  private val weekdayCompSingleVal: P[WeekdayComponent.WeekdayVal] =
    anyWeekday.map(WeekdayComponent.WeekdayVal.Single.apply)

  private val weekdayCompRangeVal: P[WeekdayComponent.WeekdayVal] =
    weekdayRange.map(WeekdayComponent.WeekdayVal.Range.apply)

  val weekdayCompVal: P[WeekdayComponent.WeekdayVal] =
    weekdayCompRangeVal | weekdayCompSingleVal

  val weekdayComponentList: P[WeekdayComponent] =
    repsep(weekdayCompVal, comma).map(v => WeekdayComponent.List(v.toSeq))

  def weekday(wd: Weekday): P[Weekday] =
    (iconst(wd.longName) | iconst(wd.shortName)).map(_ => wd)

  val zoneId: P[ZoneId] = {
    val all = ZoneId.getAvailableZoneIds()
    rest.emap { id =>
      Option(ZoneId.SHORT_IDS.get(id)) match {
        case Some(zid) => Right(ZoneId.of(zid))
        case None =>
          if (all.contains(id)) Right(ZoneId.of(id))
          else Left(s"Unknown zone id: $id")
      }
    }
  }

  def repsep[A](p: P[A], sep: P[Unit]): P[Vector[A]] =
    (rep(p <~ sep) ~ p).map { case (list, el) =>
      list :+ el
    }

  def rep[A](p: P[A]): P[Vector[A]] = {
    @annotation.tailrec
    def go(result: Vector[A], in: String): Either[String, (String, Vector[A])] =
      p.run(in) match {
        case Right(rest, a) => go(result :+ a, rest)
        case Left(_)        => Right(in -> result)
      }

    P(str => go(Vector.empty, str))
  }

  def take(n: Int): P[String] =
    P(str =>
      if (str.length >= n) Right(str.drop(n) -> str.take(n))
      else Left(s"Cannot take $n chars, input too short")
    )

  def chars(cs: Seq[Char]): P[Char] = {
    val set = cs.toSet
    P(str =>
      if (str.nonEmpty) {
        val c = str.charAt(0)
        if (set.contains(c)) Right(str.drop(1) -> c)
        else Left(s"Expected char from $set, but got $c")
      } else Left(s"Expected char from $set, but got empty string")
    )
  }

  def const(s: String): P[String] =
    P(str =>
      if (str.startsWith(s)) Right(str.substring(s.length) -> s)
      else Left(s"Expected $s, but got $str")
    )

  def iconst(s: String): P[String] =
    P(str =>
      if (str.length >= s.length) {
        val cut = str.substring(0, s.length)
        if (s.equalsIgnoreCase(cut)) Right(str.substring(s.length) -> cut)
        else Left(s"Expected $s (ignore case), but got $str")
      } else Left(s"Expected $s (ignore case), but input too short")
    )

  def rest: P[String] =
    P(str => Right("" -> str))

  def unit[A](a: A): P[A] =
    P(str => Right(str -> a))

  final case class P[A](run: String => Either[String, (String, A)]) {

    def map[B](f: A => B): P[B] =
      P(str => run(str).map { case (rest, a) => (rest, f(a)) })

    def emap[B](f: A => Either[String, B]): P[B] =
      P(str => run(str).flatMap { case (rest, a) => f(a).map(b => (rest, b)) })

    def opt: P[Option[A]] =
      P(str =>
        run(str) match {
          case Right(rest, a) => Right(rest -> Some(a))
          case Left(_)        => Right(str -> None)
        }
      )

    def |(alt: P[A]): P[A] =
      P(str =>
        run(str) match {
          case r @ Right(_) => r
          case Left(_)      => alt.run(str)
        }
      )

    def ~[B](p: P[B]): P[(A, B)] =
      P(str =>
        run(str) match {
          case Right(rest, a) =>
            p.run(rest).map { case (r, b) => (r, (a, b)) }
          case Left(err) =>
            Left(err)
        }
      )

    def <~[B](p: P[B]): P[A] =
      (this ~ p).map(_._1)

    def ~>[B](p: P[B]): P[B] =
      (this ~ p).map(_._2)

    def drain: P[Unit] =
      map(_ => ())
  }

  private def readInt(str: String): Either[String, Int] =
    catchNonFatal(str.toInt)

  private def catchNonFatal[A](code: => A): Either[String, A] =
    try Right(code)
    catch {
      case NonFatal(e) => Left(e.getMessage)
    }
}
