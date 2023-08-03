package com.github.eikek.calev.doobie

import _root_.doobie._
import _root_.doobie.implicits._
import cats.effect._
import cats.effect.unsafe.implicits._
import com.github.eikek.calev._
import munit._

class CalevDoobieTest extends FunSuite with CalevDoobieMeta {

  val xa = Transactor.fromDriverManager[IO](
    driver = "org.h2.Driver",
    url = "jdbc:h2:mem:testing;DB_CLOSE_DELAY=-1",
    user = "sa",
    password = "",
    logHandler = None
  )

  case class Record(event: CalEvent)

  def insertRecord(r: Record) = {
    val createTable = sql"""
       create table events(
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        event VARCHAR(255) NOT NULL
      )
      """

    val insertRecord = sql"""insert into events(event) values(${r.event}) """

    for {
      _ <- createTable.update.run
      id <- insertRecord.update.withUniqueGeneratedKeys[Long]("id")
    } yield id
  }

  def loadRecord(id: Long): ConnectionIO[Record] =
    sql"SELECT event FROM events WHERE id = $id".query[Record].unique

  test("insert event") {
    val record = Record(CalEvent.unsafe("Mon *-*-* 0/2:0"))

    val op = for {
      id <- insertRecord(record)
      load <- loadRecord(id)
    } yield load

    val loaded = op.transact(xa).unsafeRunSync()
    assertEquals(loaded, record)
  }

}
