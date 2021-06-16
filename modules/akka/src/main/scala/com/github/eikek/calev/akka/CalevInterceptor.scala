package com.github.eikek.calev.akka

import java.time.{Clock, ZonedDateTime}

import scala.reflect.ClassTag

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{Behavior, BehaviorInterceptor, TypedActorContext}
import com.github.eikek.calev.CalEvent
import com.github.eikek.calev.akka.CalevActorScheduling.ConfigOps

private[akka] class CalevInterceptor[B, T <: B: ClassTag](
    clock: Clock,
    calEvent: CalEvent,
    triggerFactory: ZonedDateTime => T
) extends BehaviorInterceptor[T, B] {

  override def aroundStart(
      ctx: TypedActorContext[T],
      target: BehaviorInterceptor.PreStartTarget[B]
  ): Behavior[B] =
    scheduleUpcoming(target.start(ctx))

  override def aroundReceive(
      ctx: TypedActorContext[T],
      msg: T,
      target: BehaviorInterceptor.ReceiveTarget[B]
  ): Behavior[B] =
    scheduleUpcoming(target(ctx, msg))

  private def scheduleUpcoming(target: Behavior[B]): Behavior[B] = Behaviors.setup {
    ctx =>
      val config      = ctx.system.settings.config
      val minInterval = config.getDurationMillis("akka.scheduler.tick-duration") * 4

      CalevTimerScheduler.withCalevTimers(Some(minInterval), clock) { scheduler =>
        scheduler.scheduleUpcoming(calEvent, triggerFactory)
        target
      }
  }

}
