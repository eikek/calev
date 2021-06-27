package com.github.eikek.fs2calev

import java.time._

import scala.concurrent.ExecutionContext

import cats.effect._
import com.github.eikek.calev._
import fs2.Stream
import munit._

class CalevFs2Test extends FunSuite {

  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)
  private val evalInstantNow    = Stream.eval(IO(Instant.now()))

  test("awake") {
    val evenSeconds = CalEvent.unsafe("*-*-* *:*:0/2")
    val io          = CalevFs2.awakeEvery[IO](evenSeconds) >> evalInstantNow
    val times       = io.map(_.getEpochSecond).take(2).forall(_ % 2 == 0).compile.last
    assertEquals(times.unsafeRunSync(), Option(true))
  }

  test("nextElapses") {
    val ref  = zdt(2020, 3, 6, 1, 47, 12).withZoneSameLocal(CalEvent.UTC)
    val ce   = CalEvent.unsafe("*-*-* 0/2:0:0")
    val next = CalevFs2.nextElapses[IO](ref)(ce).take(3).compile.toVector.unsafeRunSync()
    assertEquals(
      next,
      Vector(zdt(2020, 3, 6, 2, 0, 0), zdt(2020, 3, 6, 4, 0, 0), zdt(2020, 3, 6, 6, 0, 0))
    )
  }

  private def zdt(y: Int, month: Int, d: Int, h: Int, min: Int, sec: Int): ZonedDateTime =
    ZonedDateTime.of(LocalDate.of(y, month, d), LocalTime.of(h, min, sec), CalEvent.UTC)
}
