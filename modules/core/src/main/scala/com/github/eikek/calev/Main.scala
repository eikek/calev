package com.github.eikek.calev
import java.time.ZonedDateTime

object Main {

  def main(args: Array[String]): Unit =
    args match {
      case Array("--help", _) =>
        printHelp()

      case Array("-i", n, exp) if n.matches("\\d+") =>
        CalEvent.parse(exp) match {
          case Right(ce) =>
            println(s"Normalized: ${ce.asString}")
            val next = ce.nextElapses(ZonedDateTime.now, n.toInt)
            next.map(zd => s"Next: $zd").foreach(println)

          case Left(err) =>
            println("Error parsing calendar event:")
            println(err)
        }

      case Array(exp) =>
        CalEvent.parse(exp) match {
          case Right(ce) =>
            val next = ce.nextElapse(ZonedDateTime.now())
            println(s"Normalized: ${ce.asString}")
            println(s"Next: $next")

          case Left(err) =>
            println("Error parsing calendar event:")
            println(err)
        }

      case _ =>
        if (args.isEmpty) {
          println("Error: No arguments specified!")
          printHelp()
        } else {
          println(s"Invalid arguments: ${args.mkString(", ")}")
        }
    }

  def printHelp(): Unit =
    println("""
Reads in systemd-like calendar events and prints the next iterations.

Usage: cmd [-i <num>] <calendar event>

Options:
   -i <num>     Print `num' iterations for the calendar event based on the
                current time. Default is 1.

Arguments:
  The calendar event string. For example:

     Mon,Wed *-*-* 12:00:00
""")

}
