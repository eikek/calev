package com.github.eikek.calev

import minitest._
import cats.effect._
import scala.concurrent.ExecutionContext
import java.util.concurrent.Executors
import scala.concurrent.duration._

object TriggerDataTest extends SimpleTestSuite {
  implicit val CS = IO.contextShift(ExecutionContext.global)

  val resource = "trigger-data.txt"

  val data = Blocker[IO].use { blocker =>
    TestDataSet
      .readResource[IO](resource, blocker)
      .zipWithIndex
      .compile
      .toVector
  }.unsafeRunSync

  data.foreach { case (data, index) =>
    test(s"test dataset $index") {
      val num   = data.expect.size
      val nexts = data.event.nextElapses(data.ref, num).toList
      assertEquals(nexts, data.expect)
    }
  }
}
