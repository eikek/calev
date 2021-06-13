package com.github.eikek.calev.akka

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors.{receiveMessage, same}
import com.github.eikek.calev.CalEvent

import java.time.{Clock, ZonedDateTime}

case class Ping(timestamp: ZonedDateTime)

object CalevTimerSchedulerTest extends App {
  val calEvent = CalEvent.unsafe("*-*-* *:0/1:0")

  val behavior = CalevTimerScheduler.withCalendarEvent(Clock.systemUTC(), calEvent, Ping)(
    receiveMessage[Ping] { case Ping(ts) =>
      println("Ping received at: " + ts)
      same
    }
  )

  val as = ActorSystem(behavior, "ping")

}
