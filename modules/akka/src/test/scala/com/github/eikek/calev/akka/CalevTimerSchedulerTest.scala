package com.github.eikek.calev.akka

import akka.actor.testkit.typed.scaladsl.{ManualTime, ScalaTestWithActorTestKit, TestProbe}
import akka.actor.typed.scaladsl.Behaviors.{receiveMessage, same}
import com.github.eikek.calev.CalEvent
import org.scalatest.wordspec.AnyWordSpecLike
import org.slf4j.LoggerFactory

import java.time.temporal.ChronoField
import java.time.{LocalTime, ZonedDateTime}
import scala.concurrent.duration.{DurationInt, FiniteDuration}

case class Tick(timestamp: ZonedDateTime)

class CalevTimerSchedulerTest
    extends ScalaTestWithActorTestKit(ManualTime.config)
    with AnyWordSpecLike {

  val log        = LoggerFactory.getLogger(getClass)
  val calEvent   = CalEvent.unsafe("*-*-* *:0/1:0")
  val manualTime = ManualTime()
  val clock      = new TestClock

  "Akka Timer" should {
    val probe = TestProbe[Tick]()

    "trigger periodically according to given CalEvent" in {
      val behavior = CalevTimerScheduler.withCalendarEvent(clock, calEvent, Tick)(
        receiveMessage[Tick] { tick =>
          probe.ref ! tick
          log.info(s"Tick scheduled at ${tick.timestamp.toLocalTime} received at: ${LocalTime.now(clock)}")
          same
        }
      )

      spawn(behavior)

      expectNoMessagesFor((60 - LocalTime.now().get(ChronoField.SECOND_OF_MINUTE)).seconds - 1.second)

      timePasses(2.seconds)
      probe.expectMessageType[Tick]

      expectNoMessagesFor(30.seconds)

      timePasses(30.seconds)
      probe.expectMessageType[Tick]

      expectNoMessagesFor(30.seconds)
    }

    def expectNoMessagesFor(duration: FiniteDuration): Unit = {
      log.debug("Expect no messages for {}", duration)
      manualTime.expectNoMessageFor(100.millis, probe)
      timePasses(duration)
      manualTime.expectNoMessageFor(100.millis, probe)
    }

    def timePasses(duration: FiniteDuration): Unit = {
      clock.tick(duration)
      manualTime.timePasses(duration)
    }
  }

}
