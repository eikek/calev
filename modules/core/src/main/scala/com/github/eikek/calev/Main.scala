package com.github.eikek.calev

import java.time.LocalDateTime

object Main {

  case class Opts(
      pattern: Option[CalEvent],
      reps: Int,
      ref: LocalDateTime,
      help: Boolean
  )

  def main(args: Array[String]): Unit =
    readArgs(args) match {
      case Opts(_, _, _, true) =>
        printHelp()

      case Opts(Some(ce), reps, ref, _) =>
        println(s"Normalized:  ${ce.asString}")
        println(s"Reference:   $ref")
        println("Next Elapses:")
        val next = ce.nextElapses(ref, reps)
        next.foreach(zd => println(s"- $zd"))

      case _ =>
        printHelp()
    }

  def readArgs(args: Array[String]): Opts = {
    def readOpts(in: List[(String, String)], opts: Opts): Opts =
      in match {
        case Nil => opts
        case name, value :: rest =>
          name match {
            case "-i"    => readOpts(rest, opts.copy(reps = value.toInt))
            case "--ref" => readOpts(rest, opts.copy(ref = LocalDateTime.parse(value)))
            case _       => sys.error(s"Unknown option: $name")
          }
        case null =>
          opts
      }

    val first = args.headOption
    if (first == None || first == Some("--help") || first == Some("-h"))
      Opts(None, 1, LocalDateTime.now, true)
    else
      CalEvent.parse(args.last) match {
        case Right(ce) =>
          val options = args
            .dropRight(1)
            .grouped(2)
            .filter(_.length == 2)
            .map(arr => (arr(0), arr(1)))
            .toList
          readOpts(options, Opts(Some(ce), 1, LocalDateTime.now(), false))
        case Left(err) =>
          sys.error(s"Cannot read timer ${args.last}: $err")
      }
  }

  def printHelp(): Unit =
    println("""
Reads in systemd-like calendar events and prints the next iterations.

Usage: cmd [-i <num>] [--ref <datetime>] <calendar event>

Options:
   -i <num>
                Print `num' iterations for the calendar event based on the
                current time. Default is 1.
   --ref <datetime>
                Use the given datetime as reference and not the current
                time.

Arguments:
  The calendar event string. For example:

     Mon,Wed *-*-* 12:00:00
""")

}
