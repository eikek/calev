package com.github.eikek.calev

sealed trait Value {
  def contains(n: Int): Boolean

  def asString: String

  def validate(min: Int, max: Int): Seq[String]
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
  }

  case class Range(start: Int, end: Int, rep: Option[Int]) extends Value {
    require(start <= end, "start must be <= end")

    def contains(n: Int): Boolean =
      rep match {
        case None =>
          n >= start && n <= end

        case Some(m) =>
          val i1 = math.abs((n - start) / m)
          val i2 = math.abs((n - end) / m)
          ((start + i1 * m) <= n && n <= (end + i1 * m)) ||
          ((start + i2 * m) <= n && n <= (end + i2 * m))
      }

    def asString: String = rep match {
      case None    => f"$start%02d..$end%02d"
      case Some(r) => f"$start%02d..$end%02d/$r"
    }

    def validate(min: Int, max: Int): Seq[String] = {
      val errs = Single(start, None).validate(min, max) ++
        Single(end, None).validate(min, max)

      if (min < max) errs
      else errs ++ Seq(s"Range invalid: $min >= $max")
    }
  }

  def apply(n: Int): Value =
    Single(n, None)

  def range(start: Int, end: Int): Value =
    Range(start, end, None)

}
