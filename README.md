# calev
[![Build Status](https://travis-ci.com/eikek/calev.svg?branch=master)](https://travis-ci.com/eikek/calev)
[![Scaladex](https://index.scala-lang.org/eikek/calev/latest.svg?color=blue)](https://index.scala-lang.org/eikek/calev/calev-core)


Small library for parsing systemd calendar event expressions. See

``` shell
man systemd.time
```

or

<https://man.cx/systemd.time#heading7>

## Simplified

This library has some limitations when parsing calendar events
compared to systemd:

- The `~` in the date part for refering last days of a month is not
  supported.
- No parts except weekdays may be absent, date and time parts must be
  specified

## Modules

- The *core* module has zero dependencies and implements the parser
  and generator for calendar events. With sbt, use:
  ```sbt
  libraryDependencies += "com.github.eikek" %% "calev-core" % "0.1.0-SNAPSHOT"
  ```
- The *fs2* module contains utilities to work with
  [FS2](https://github.com/functional-streams-for-scala/fs2) streams.
  These were taken, thankfully and slightly modified to exchange cron expressions
  for calendar events, from the
  [fs2-cron](https://github.com/fthomas/fs2-cron) library. With sbt, use
  ```sbt
  libraryDependencies += "com.github.eikek" %% "calev-fs2" % "0.1.0-SNAPSHOT"
  ```


## Examples

### Core

Calendar events can be read from a string:

```scala
import com.github.eikek.calev._

CalEvent.parse("Mon..Fri *-*-* 6,14:0:0")
// res0: Either[String, CalEvent] = Right(
//   CalEvent(
//     List(Vector(Range(WeekdayRange(Mon, Fri)))),
//     DateEvent(All, All, All),
//     TimeEvent(
//       List(Vector(Single(6, None), Single(14, None))),
//       List(Vector(Single(0, None))),
//       List(Vector(Single(0, None)))
//     ),
//     None
//   )
// )

CalEvent.parse("Mon *-*-* 6,88:0:0")
// res1: Either[String, CalEvent] = Left("Value 88 not in range [0,23]")
```

There is an `unsafe` way that throws exceptions:

```scala
CalEvent.unsafe("*-*-* 0/2:0:0")
// res2: CalEvent = CalEvent(
//   All,
//   DateEvent(All, All, All),
//   TimeEvent(
//     List(Vector(Single(0, Some(2)))),
//     List(Vector(Single(0, None))),
//     List(Vector(Single(0, None)))
//   ),
//   None
// )
```

There is a tiny dsl for more conveniently defining events in code:

```scala
import com.github.eikek.calev.Dsl._

val ce = CalEvent(AllWeekdays, DateEvent.All, time(0 #/ 2, 0.c, 0.c))
// ce: CalEvent = CalEvent(
//   All,
//   DateEvent(All, All, All),
//   TimeEvent(
//     List(List(Single(0, Some(2)))),
//     List(List(Single(0, None))),
//     List(List(Single(0, None)))
//   ),
//   None
// )
ce.asString
// res3: String = "*-*-* 00/2:00:00"
```

Once there is a calendar event, the times it will elapse next can be
generated:

```scala
import java.time._

ce.asString
// res4: String = "*-*-* 00/2:00:00"
val now = LocalDateTime.now
// now: LocalDateTime = 2020-03-07T12:45:39.939
ce.nextElapse(now)
// res5: Option[LocalDateTime] = Some(2020-03-07T14:00)
ce.nextElapses(now, 5)
// res6: List[LocalDateTime] = List(
//   2020-03-07T14:00,
//   2020-03-07T16:00,
//   2020-03-07T18:00,
//   2020-03-07T20:00,
//   2020-03-07T22:00
// )
```

If an event is in the past, the `nextElapsed` returns a `None`:

```scala
CalEvent.unsafe("1900-01-* 12,14:0:0").nextElapse(LocalDateTime.now)
// res7: Option[LocalDateTime] = None
```


### FS2

The fs2 utilities allow to schedule things based on calendar events.
This is the same as [fs2-cron](https://github.com/fthomas/fs2-cron)
provides, only adopted to use calendar events instead of cron
expressions. The example is also from there.

```scala
import cats.effect.{IO, Timer}
import fs2.Stream
import com.github.eikek.fs2calev._
import java.time.LocalTime
import scala.concurrent.ExecutionContext

implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)
// timer: Timer[IO] = cats.effect.internals.IOTimer@782926a1

val printTime = IO(println(LocalTime.now))
// printTime: IO[Unit] = Delay(<function0>)

val event = CalEvent.unsafe("*-*-* *:*:0/2")
// event: CalEvent = CalEvent(
//   All,
//   DateEvent(All, All, All),
//   TimeEvent(All, All, List(Vector(Single(0, Some(2))))),
//   None
// )

val task = CalevFs2.awakeEvery[IO](event).evalMap(_ => printTime)
// task: Stream[IO[x], Unit] = Stream(..)

task.take(3).compile.drain.unsafeRunSync
// 12:45:42.023
// 12:45:44.001
// 12:45:46.001
```
