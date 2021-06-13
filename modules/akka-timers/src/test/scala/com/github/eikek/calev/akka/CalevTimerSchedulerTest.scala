package com.github.eikek.calev.akka

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors.{receiveMessage, same}
import com.github.eikek.calev.CalEvent

import java.time.{Clock, ZonedDateTime}

case class Ping(timestamp: ZonedDateTime)

object CalevTimerSchedulerTest extends App {
  val calEvent = CalEvent.unsafe("*-*-* *:0/1:0")
  val clock = Clock.systemUTC()

  val behavior = CalevTimerScheduler.withCalendarEvent(clock, calEvent, Ping)(
    receiveMessage[Ping] { case Ping(ts) =>
      println(s"Ping scheduled at $ts received at: ${clock.instant()}")
      same
    }
  )

  val as = ActorSystem(behavior, "ping")

}
