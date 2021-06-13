package com.github.eikek.calev.akka

import akka.actor.typed.{Behavior, BehaviorInterceptor, TypedActorContext}
import com.github.eikek.calev.CalEvent

import java.time.{Clock, ZonedDateTime}
import scala.reflect.ClassTag

private[akka] class CalevInterceptor[B, T <: B : ClassTag](
    clock: Clock,
    calEvent: CalEvent,
    triggerFactory: ZonedDateTime => T
) extends BehaviorInterceptor[T, B] {

  def aroundReceive(
      ctx: TypedActorContext[T],
      msg: T,
      target: BehaviorInterceptor.ReceiveTarget[B]
  ): Behavior[B] =
    CalevTimerScheduler.withCalevTimers(clock) { scheduler =>
      scheduler.scheduleUpcoming(calEvent, triggerFactory)
      target(ctx, msg)
    }

}
