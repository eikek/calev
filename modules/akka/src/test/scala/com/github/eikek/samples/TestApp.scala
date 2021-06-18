package com.github.eikek.samples

import java.time.ZonedDateTime

import akka.actor.Cancellable
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.{ActorRef, ActorSystem}
import com.github.eikek.calev.CalEvent
import com.github.eikek.calev.akka._

object TestApp {
  sealed trait Message
  case class Tick(timestamp: ZonedDateTime) extends Message
  case class Ping()                         extends Message

  val actorCtx: ActorContext[_]   = null
  val calEvent                    = CalEvent.unsafe("*-*-* *:0/1:0") // every day, every full minute
  val clock                       = new TestClock
  val actorRef: ActorRef[Message] = null
  val as: ActorSystem[_]          = null
  val r: Option[Cancellable]      = actorCtx.scheduleUpcoming(calEvent, actorRef, Tick)

}
