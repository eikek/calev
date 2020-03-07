package com.github.eikek.calev

final case class DateEvent(
    year: Component,
    month: Component,
    day: Component
)

object DateEvent {

  val All = DateEvent(Component.All, Component.All, Component.All)

}
