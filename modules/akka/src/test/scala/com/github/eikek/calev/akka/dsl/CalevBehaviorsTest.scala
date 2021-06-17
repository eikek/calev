package com.github.eikek.calev.akka.dsl

import java.time.temporal.ChronoField
import java.time.{LocalTime, ZonedDateTime}

import scala.concurrent.duration.{DurationInt, FiniteDuration}

import akka.actor.testkit.typed.scaladsl.{
  ManualTime,
  ScalaTestWithActorTestKit,
  TestProbe
}
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.Behaviors.{receiveMessage, same}
import com.github.eikek.calev.CalEvent
import com.github.eikek.calev.akka.TestClock
import com.github.eikek.calev.akka.dsl.CalevBehaviorsTest._
import org.scalatest.wordspec.AnyWordSpecLike
import org.slf4j.LoggerFactory

object CalevBehaviorsTest {
  sealed trait Message
  case class Tick(timestamp: ZonedDateTime) extends Message
  case class Ping()                         extends Message
}

class CalevBehaviorsTest
    extends ScalaTestWithActorTestKit(ManualTime.config)
    with AnyWordSpecLike {

  val log        = LoggerFactory.getLogger(getClass)
  val manualTime = ManualTime()
  val clock      = new TestClock

  "Akka Timer" should {
    val probe = TestProbe[Message]()

    "trigger periodically according to given CalEvent" in {

      // every day, every full minute
      val calEvent = CalEvent.unsafe("*-*-* *:0/1:0")

      val behavior = CalevBehaviors.withCalendarEvent(calEvent, clock)(
        Tick,
        receiveMessage[Message] {
          case tick: Tick =>
            probe.ref ! tick
            log.info(
              s"Tick scheduled at ${tick.timestamp.toLocalTime} received at: ${LocalTime.now(clock)}"
            )
            same
          case ping: Ping =>
            probe.ref ! ping
            log.info("Ping received")
            same
        }
      )

      val actor: ActorRef[Message] = spawn(behavior)

      actor ! Ping()
      probe.expectMessage(Ping())

      expectNoMessagesFor(
        (60 - LocalTime.now().get(ChronoField.SECOND_OF_MINUTE)).seconds - 1.second
      )

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
