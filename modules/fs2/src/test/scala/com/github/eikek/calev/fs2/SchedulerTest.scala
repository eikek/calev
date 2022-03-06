package com.github.eikek.calev.fs2

import java.time.{Instant, ZoneId, ZoneOffset}

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.github.eikek.calev.CalEvent
import fs2.Stream
import munit.FunSuite

class SchedulerTest extends FunSuite {
  private val evenSeconds: CalEvent = CalEvent.unsafe("*-*-* *:*:0/2")
  private def isEven(i: Long): Boolean = i % 2 == 0
  private def instantSeconds(i: Instant): Long = i.getEpochSecond
  private val evalInstantNow: Stream[IO, Instant] = Stream.eval(IO(Instant.now()))

  private val schedulerSys = Scheduler.systemDefault[IO]
  private val schedulerUtc = Scheduler.utc[IO]

  test("awakeEvery") {
    val s1 = schedulerSys.awakeEvery(evenSeconds) >> evalInstantNow
    val s2 = s1.map(instantSeconds).take(2).forall(isEven)
    s2.compile.last.map(a => assertEquals(a, Option(true))).unsafeRunSync()
  }

  test("sleep") {
    val s1 = schedulerUtc.sleep(evenSeconds) >> evalInstantNow
    val s2 = s1.map(instantSeconds).forall(isEven)
    s2.compile.last.map(a => assertEquals(a, Option(true))).unsafeRunSync()
  }

  test("timezones") {
    val zoneId: ZoneId = ZoneOffset.ofTotalSeconds(1)
    val scheduler = Scheduler.from(IO.pure(zoneId))

    val s1 = scheduler.awakeEvery(evenSeconds) >> evalInstantNow
    val s2 = s1.map(instantSeconds).take(2).forall(!isEven(_))
    s2.compile.last.map(a => assertEquals(a, Option(true))).unsafeRunSync()
  }
}
