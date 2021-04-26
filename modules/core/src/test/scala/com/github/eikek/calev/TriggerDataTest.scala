package com.github.eikek.calev

import scala.concurrent.ExecutionContext

import cats.effect._
import minitest._

object TriggerDataTest extends SimpleTestSuite {
  implicit val CS = IO.contextShift(ExecutionContext.global)

  val resource = "trigger-data.txt"

  val data = Blocker[IO]
    .use { blocker =>
      TestDataSet
        .readResource[IO](resource, blocker)
        .zipWithIndex
        .compile
        .toVector
    }
    .unsafeRunSync()

  data.foreach {
    case (Left(ex), index) =>
      test(s"test dataset $index") {
        throw ex
      }
    case (Right(data), index) =>
      test(s"test dataset zoned-datetime $index ${data.event.asString}/${data.ref}") {
        val num   = math.max(1, data.expect.size)
        val nexts = data.event.nextElapses(data.ref, num).toList
        assertEquals(nexts, data.expect)
      }
      test(s"test dataset local-datetime $index ${data.event.asString}/${data.ref}") {
        val num   = math.max(1, data.expect.size)
        val nexts = data.event.nextElapses(data.ref.toLocalDateTime(), num).toList
        assertEquals(nexts, data.expect.map(_.toLocalDateTime()))
      }
  }
}
