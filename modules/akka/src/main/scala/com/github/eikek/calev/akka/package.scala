package com.github.eikek.calev

import java.time.Clock

import scala.language.implicitConversions

import _root_.akka.actor.typed.ActorSystem
import _root_.akka.actor.typed.Scheduler
import _root_.akka.actor.typed.scaladsl.TimerScheduler
import com.github.eikek.calev.akka.dsl.{
  CalevActorScheduling,
  CalevScheduler,
  CalevTimerScheduler
}
import com.github.eikek.calev.akka.internal.{CalevSchedulerImpl, CalevTimerSchedulerImpl}

package object akka extends CalevActorScheduling {

  def calevScheduler(clock: Clock = Clock.systemDefaultZone())(implicit
      actorSystem: ActorSystem[_]
  ): CalevScheduler =
    new CalevSchedulerImpl(actorSystem.scheduler, clock)

  implicit def toAkkaScheduler(calevScheduler: CalevScheduler): Scheduler =
    calevScheduler.asInstanceOf[CalevSchedulerImpl].scheduler

  implicit def toAkkaTimerScheduler[T](
      calevScheduler: CalevTimerScheduler[T]
  ): TimerScheduler[T] =
    calevScheduler.asInstanceOf[CalevTimerSchedulerImpl[T]].scheduler

}
