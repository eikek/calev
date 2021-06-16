package com.github.eikek.calev.akka

import java.time.{Clock, ZonedDateTime}

import scala.reflect.ClassTag

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{Behavior, BehaviorInterceptor, TypedActorContext}
import com.github.eikek.calev.CalEvent
import com.github.eikek.calev.akka.CalevActorScheduling.ConfigOps

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
      val config      = ctx.system.settings.config
      val minInterval = config.getDurationMillis("akka.scheduler.tick-duration") * 4

      CalevTimerScheduler.withCalevTimers(Some(minInterval), clock) { scheduler =>
        scheduler.scheduleUpcoming(calEvent, triggerFactory)
        target
      }
  }

}
