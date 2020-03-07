package com.github.eikek.calev

final case class TimeEvent(
    hour: Component,
    minute: Component,
    seconds: Component
)

object TimeEvent {

  val All = TimeEvent(Component.All, Component.All, Component.All)
}
