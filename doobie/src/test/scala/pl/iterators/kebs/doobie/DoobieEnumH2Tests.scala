package pl.iterators.kebs.doobie

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.apply._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.typelevel.doobie._
import org.typelevel.doobie.implicits._
import pl.iterators.kebs.core.enums.EnumNaming
import pl.iterators.kebs.enumeratum.{KebsEnumeratum, KebsValueEnumeratum}
import pl.iterators.kebs.testkit.model.{Color, Level}

class DoobieEnumH2Tests extends AnyFunSuite with Matchers with KebsEnumeratum with KebsValueEnumeratum {
  private def transactor(db: String): Transactor[IO] =
    Transactor.fromDriverManager[IO]("org.h2.Driver", s"jdbc:h2:mem:$db;DB_CLOSE_DELAY=-1", "sa", "", None)

  private def stored(db: String)(insert: ConnectionIO[Int]): String =
    (sql"create table t (c varchar(32))".update.run *> insert *> sql"select c from t".query[String].unique)
      .transact(transactor(db))
      .unsafeRunSync()

  private def read[A](db: String, raw: String)(implicit get: Get[A]): A =
    (sql"create table t (c varchar(32))".update.run *> sql"insert into t values ($raw)".update.run *> sql"select c from t".query[A].unique)
      .transact(transactor(db))
      .unsafeRunSync()

  test("enum names stored and read back") {
    import pl.iterators.kebs.doobie.enums._
    stored("default")(sql"insert into t values (${Color.DarkBlue: Color})".update.run) shouldBe "DarkBlue"
    read[Color]("default-read", "DarkBlue") shouldBe Color.DarkBlue
  }

  test("uppercase enum names stored and read back") {
    import pl.iterators.kebs.doobie.enums.uppercase._
    stored("upper")(sql"insert into t values (${Color.DarkBlue: Color})".update.run) shouldBe "DARKBLUE"
    read[Color]("upper-read", "DARKBLUE") shouldBe Color.DarkBlue
  }

  test("lowercase enum names stored and read back") {
    import pl.iterators.kebs.doobie.enums.lowercase._
    stored("lower")(sql"insert into t values (${Color.DarkBlue: Color})".update.run) shouldBe "darkblue"
    read[Color]("lower-read", "darkblue") shouldBe Color.DarkBlue
  }

  test("an EnumNaming in scope renames stored names") {
    import pl.iterators.kebs.doobie.enums._
    implicit val enumNaming: EnumNaming = EnumNaming.screamingSnakeCase
    stored("snake")(sql"insert into t values (${Color.DarkBlue: Color})".update.run) shouldBe "DARK_BLUE"
    read[Color]("snake-read", "dark_blue") shouldBe Color.DarkBlue
  }

  test("names are read case-insensitively") {
    import pl.iterators.kebs.doobie.enums._
    read[Color]("lenient-read", "darkblue") shouldBe Color.DarkBlue
  }

  test("value enums store their values") {
    import pl.iterators.kebs.doobie.enums._
    val value = (sql"create table v (l int)".update.run *> sql"insert into v values (${Level.High: Level})".update.run *>
      sql"select l from v".query[Int].unique).transact(transactor("value")).unsafeRunSync()
    value shouldBe 2
  }
}
