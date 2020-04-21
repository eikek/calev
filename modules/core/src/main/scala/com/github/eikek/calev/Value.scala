package com.github.eikek.calev

sealed trait Value {
  def contains(n: Int): Boolean

  def asString: String

  def validate(min: Int, max: Int): Seq[String]

  def expand(max: Int): Vector[Int]
}

object Value {

  case class Single(value: Int, rep: Option[Int]) extends Value {
    def contains(n: Int): Boolean =
      rep match {
        case Some(r) =>
          n >= value && (n - value) % r == 0

        case None =>
          n == value
      }

    def asString: String = rep match {
      case Some(r) => f"$value%02d/$r"
      case None    => f"$value%02d"
    }

    def validate(min: Int, max: Int): Seq[String] =
      if (min <= value && value <= max) Nil
      else Seq(s"Value $value not in range [$min,$max]")

    def expand(max: Int): Vector[Int] =
      rep match {
        case Some(r) =>
          @annotation.tailrec
          def go(v: Vector[Int], count: Int): Vector[Int] =
            if (count * r + value > max) v
            else go(v :+ (count * r + value), count + 1)

          go(Vector.empty, 0)

        case None =>
          Vector(value)
      }
  }

  case class Range(start: Int, end: Int, rep: Option[Int]) extends Value {

    def contains(n: Int): Boolean =
      n >= start && n <= end &&
        (rep == None || Single(start, rep).contains(n))

    def asString: String = rep match {
      case None    => f"$start%02d..$end%02d"
      case Some(r) => f"$start%02d..$end%02d/$r"
    }

    def validate(min: Int, max: Int): Seq[String] = {
      val errs = Single(start, None).validate(min, max) ++
        Single(end, None).validate(min, max)

      val rangeErrs =
        if (start > end) Seq(s"Range invalid: $start (start) > $end (end)")
        else Seq.empty

      val subErrs =
        if (min < max) Seq.empty
        else Seq(s"Subrange invalid: $min >= $max")

      errs ++ rangeErrs ++ subErrs
    }

    def expand(max: Int): Vector[Int] =
      rep match {
        case Some(_) =>
          val newMax = math.min(end, max)
          Single(start, rep).expand(newMax)

        case None =>
          (start to math.min(end, max)).toVector
      }
  }

  def apply(n: Int): Value =
    Single(n, None)

  def range(start: Int, end: Int): Value =
    Range(start, end, None)

}
