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
  libraryDependencies += "com.github.eikek" %% "calev-core" % "@VERSION@"
  ```
- The *fs2* module contains utilities to work with
  [FS2](https://github.com/functional-streams-for-scala/fs2) streams.
  These were taken, thankfully and slightly modified to exchange cron expressions
  for calendar events, from the
  [fs2-cron](https://github.com/fthomas/fs2-cron) library. With sbt, use
  ```sbt
  libraryDependencies += "com.github.eikek" %% "calev-fs2" % "@VERSION@"
  ```


## Examples

### Core

Calendar events can be read from a string:

```scala mdoc
import com.github.eikek.calev._

CalEvent.parse("Mon..Fri *-*-* 6,14:0:0")

CalEvent.parse("Mon *-*-* 6,88:0:0")
```

There is an `unsafe` way that throws exceptions:

```scala mdoc
CalEvent.unsafe("*-*-* 0/2:0:0")
```

There is a tiny dsl for more conveniently defining events in code:

```scala mdoc
import com.github.eikek.calev.Dsl._

val ce = CalEvent(AllWeekdays, DateEvent.All, time(0 #/ 2, 0.c, 0.c))
ce.asString
```

Once there is a calendar event, the times it will elapse next can be
generated:

```scala mdoc
import java.time._

ce.asString
val now = LocalDateTime.now
ce.nextElapse(now)
ce.nextElapses(now, 5)
```

If an event is in the past, the `nextElapsed` returns a `None`:

```scala mdoc
CalEvent.unsafe("1900-01-* 12,14:0:0").nextElapse(LocalDateTime.now)
```


### FS2

The fs2 utilities allow to schedule things based on calendar events.
This is the same as [fs2-cron](https://github.com/fthomas/fs2-cron)
provides, only adopted to use calendar events instead of cron
expressions. The example is also from there.

```scala mdoc
import cats.effect.{IO, Timer}
import fs2.Stream
import com.github.eikek.fs2calev._
import java.time.LocalTime
import scala.concurrent.ExecutionContext

implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

val printTime = IO(println(LocalTime.now))

val event = CalEvent.unsafe("*-*-* *:*:0/2")

val task = CalevFs2.awakeEvery[IO](event).evalMap(_ => printTime)

task.take(3).compile.drain.unsafeRunSync
```
