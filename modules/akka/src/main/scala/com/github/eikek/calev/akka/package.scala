package com.github.eikek.calev

import java.time.Clock
import _root_.akka.actor.typed.Scheduler
import _root_.akka.actor.typed.scaladsl.TimerScheduler
import com.github.eikek.calev.akka.dsl.{CalevActorScheduling, CalevScheduler, CalevTimerScheduler}
import com.github.eikek.calev.akka.internal.{CalevSchedulerImpl, CalevTimerSchedulerImpl}

import scala.language.implicitConversions

package object akka extends CalevActorScheduling {

  def calevScheduler(
      scheduler: Scheduler,
      clock: Clock = Clock.systemDefaultZone()
  ): CalevScheduler =
    new CalevSchedulerImpl(scheduler, clock)

  implicit def toAkkaScheduler(calevScheduler: CalevScheduler): Scheduler =
    calevScheduler.asInstanceOf[CalevSchedulerImpl].scheduler

  implicit def toAkkaTimerScheduler[T](calevScheduler: CalevTimerScheduler[T]): TimerScheduler[T] =
    calevScheduler.asInstanceOf[CalevTimerSchedulerImpl[T]].scheduler

}
