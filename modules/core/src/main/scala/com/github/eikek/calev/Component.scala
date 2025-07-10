package com.github.eikek.calev

import scala.collection.immutable.Seq
sealed trait Component {

  def contains(n: Int): Boolean

  def ++(c: Component): Component

  def asString: String

  def validate(min: Int, max: Int): Seq[String]

  /** Find the first value in this component that is in range [min,max]. */
  def findFirst(min: Int, max: Int): Option[Int]
}

object Component {

  case object All extends Component {
    def contains(n: Int): Boolean = true
    def ++(c: Component): Component = this
    def asString: String = "*"
    def validate(min: Int, max: Int): Seq[String] = Nil
    def findFirst(min: Int, max: Int): Option[Int] =
      Some(min).filter(_ <= max)
  }

  case class List(values: Seq[Value]) extends Component {
    def expanded(max: Int) =
      values.flatMap(_.expand(max)).distinct.sorted.toVector

    def contains(n: Int): Boolean =
      values.exists(_.contains(n))

    def ++(c: Component): Component =
      c match {
        case All      => All
        case List(vs) =>
          List(values ++ vs)
      }

    def asString: String =
      values.map(_.asString).mkString(",")

    def validate(min: Int, max: Int): Seq[String] =
      values.flatMap(_.validate(min, max))

    def findFirst(min: Int, max: Int): Option[Int] =
      expanded(max).find(_ >= min)
  }

  def apply(n: Int, more: Int*): Component =
    List(Seq(Value(n)) ++ more.map(m => Value(m)))

}
