package com.github.eikek.calev.akka.dsl

import java.time.ZonedDateTime

import com.github.eikek.calev.CalEvent

trait CalevTimerScheduler[T] {

  /** Start an Akka Timer that will send a message to the self actor
    * when the upcoming event occurs as defined by given calendar event.
    *
    * If a new timer is started with the same message
    * the previous is cancelled. It is guaranteed that a message from the
    * previous timer is not received, even if it was already enqueued
    * in the mailbox when the new timer was started. If you do not want this,
    * you can start start them as individual timers by specifying distinct keys.
    */
  def startSingleTimer(calEvent: CalEvent, msgFactory: ZonedDateTime => T): Unit

  /** Start an Akka Timer that will send a message to the self actor
    * when the upcoming event occurs as defined by given calendar event.
    *
    * Each timer has a key and if a new timer with same key is started
    * the previous is cancelled. It is guaranteed that a message from the
    * previous timer is not received, even if it was already enqueued
    * in the mailbox when the new timer was started.
    */
  def startSingleTimer[K](
      key: K,
      calEvent: CalEvent,
      msgFactory: (K, ZonedDateTime) => T
  ): Unit

}
