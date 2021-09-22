package com.github.eikek.calev.akka.internal

import java.time.{Clock, ZonedDateTime}

import scala.reflect.ClassTag

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{Behavior, BehaviorInterceptor, TypedActorContext}
import com.github.eikek.calev.CalEvent
import com.github.eikek.calev.akka.dsl.CalevBehaviors

private[akka] class CalevInterceptor[I, O <: I: ClassTag](
    clock: Clock,
    calEvent: CalEvent,
    triggerFactory: ZonedDateTime => O
) extends BehaviorInterceptor[O, I] {

  override def aroundStart(
      ctx: TypedActorContext[O],
      target: BehaviorInterceptor.PreStartTarget[I]
  ): Behavior[I] =
    scheduleUpcoming(target.start(ctx))

  override def aroundReceive(
      ctx: TypedActorContext[O],
      msg: O,
      target: BehaviorInterceptor.ReceiveTarget[I]
  ): Behavior[I] =
    scheduleUpcoming(target(ctx, msg))

  private def scheduleUpcoming(target: Behavior[I]): Behavior[I] = Behaviors.setup {
    ctx =>
      val config = ctx.system.settings.config
      val minInterval = config.getDurationMillis("akka.scheduler.tick-duration") * 4

      CalevBehaviors.withCalevTimers(Some(minInterval), clock) { scheduler =>
        scheduler.startSingleTimer(calEvent, triggerFactory)
        target
      }
  }

}
