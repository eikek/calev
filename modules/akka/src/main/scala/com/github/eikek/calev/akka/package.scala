package com.github.eikek.calev

import java.time.Clock

import _root_.akka.actor.typed.Scheduler
import com.github.eikek.calev.akka.dsl.{CalevActorScheduling, CalevScheduler}
import com.github.eikek.calev.akka.internal.CalevSchedulerImpl

package object akka extends CalevActorScheduling {

  def calevScheduler(
      scheduler: Scheduler,
      clock: Clock = Clock.systemDefaultZone()
  ): CalevScheduler =
    new CalevSchedulerImpl(scheduler, clock)

}
