package com.github.eikek.fs2calev

import cats.effect._
import fs2.Stream
import minitest._
import com.github.eikek.calev._
import scala.concurrent.ExecutionContext
import java.time._

object CalevFs2Test extends SimpleTestSuite {

  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

  test("awake") {
    val ce = CalEvent.unsafe("*-*-* *:*:0/2")
    val io = CalevFs2.awakeEvery[IO](ce) >> CalevFs2.evalNow[IO]
    val times = io.take(2).compile.toVector.unsafeRunSync()
    assertEquals(times.size, 2)
    assert(times.forall(dt => dt.getSecond % 2 == 0))
  }

  test("nextElapses") {
    val ref = zdt(2020, 3, 6, 1, 47, 12).withZoneSameLocal(CalEvent.UTC)
    val ce = CalEvent.unsafe("*-*-* 0/2:0:0")
    val next = CalevFs2.nextElapses[IO](ref)(ce).take(3).compile.toVector.unsafeRunSync
    assertEquals(
      next,
      Vector(zdt(2020, 3, 6, 2, 0, 0), zdt(2020, 3, 6, 4, 0, 0), zdt(2020, 3, 6, 6, 0, 0))
    )
  }

  private def zdt(y: Int, month: Int, d: Int, h: Int, min: Int, sec: Int): ZonedDateTime =
    ZonedDateTime.of(LocalDate.of(y, month, d), LocalTime.of(h, min, sec), CalEvent.UTC)
}
