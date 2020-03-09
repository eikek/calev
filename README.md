# calev
[![Build Status](https://travis-ci.org/eikek/calev.svg?branch=master)](https://travis-ci.org/eikek/calev)
[![Scaladex](https://index.scala-lang.org/eikek/calev/latest.svg?color=blue)](https://index.scala-lang.org/eikek/calev/calev-core)
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)

Small Scala library for parsing systemd.time like calendar event
expressions. It is available for Scala 2.12 and 2.13. The core module
has no dependencies.

## What are calendar events?

It serves the same purpose as cron expressions, but uses a different
syntax: a "normal" timestamp where each part is a pattern. A pattern
is a list of values, a range or `*` meaning every value. Some
examples:

| Expression                  | Meaning                                                      |
|-----------------------------|--------------------------------------------------------------|
| `*-*-* 12:15:00`            | every day at 12:15                                           |
| `2020-1,5,9-* 10:00:00`     | every day on Jan, May and Sept of 2020 at 10:00              |
| `Mon *-*-* 09:00:00`        | every monday at 9:00                                         |
| `Mon..Fri *-*-1/7 15:00:00` | on 1.,8.,15. etc of every month at 15:00 but not on weekends |

The `1/7` means value `1` and all multiples of `7` added to it.

For more information see

```shell
man systemd.time
```

or

<https://man.cx/systemd.time#heading7>


## Limitations

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
  libraryDependencies += "com.github.eikek" %% "calev-core" % "0.1.0"
  ```
- The *fs2* module contains utilities to work with
  [FS2](https://github.com/functional-streams-for-scala/fs2) streams.
  These were taken, thankfully and slightly modified to exchange cron expressions
  for calendar events, from the
  [fs2-cron](https://github.com/fthomas/fs2-cron) library. With sbt, use
  ```sbt
  libraryDependencies += "com.github.eikek" %% "calev-fs2" % "0.1.0"
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
// now: LocalDateTime = 2020-03-10T00:09:44.799
ce.nextElapse(now)
// res5: Option[LocalDateTime] = Some(2020-03-10T02:00)
ce.nextElapses(now, 5)
// res6: List[LocalDateTime] = List(
//   2020-03-10T02:00,
//   2020-03-10T04:00,
//   2020-03-10T06:00,
//   2020-03-10T08:00,
//   2020-03-10T10:00
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
// timer: Timer[IO] = cats.effect.internals.IOTimer@69cca45b

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
// 00:09:46.021
// 00:09:48
// 00:09:50
```
