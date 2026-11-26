package pl.iterators.kebs.doobie

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.apply._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.typelevel.doobie._
import org.typelevel.doobie.implicits._
import org.typelevel.doobie.util.invariant.InvalidValue
import pl.iterators.kebs.core.instances.InstanceConverter
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.testkit.validation._

class DoobieValidationTests extends AnyFunSuite with Matchers with KebsDoobie with CaseClass1ToValueClass {
  private def read[A](db: String, column: String, raw: String)(implicit get: Get[A]): A =
    (Fragment.const(s"create table t (c $column)").update.run *>
      Fragment.const(s"insert into t values ($raw)").update.run *> sql"select c from t".query[A].unique)
      .transact(Transactor.fromDriverManager[IO]("org.h2.Driver", s"jdbc:h2:mem:$db;DB_CLOSE_DELAY=-1", "sa", "", None))
      .unsafeRunSync()

  test("a value class's own from decides") {
    read[Code]("code-ok", "varchar(8)", "'abc'") shouldBe Code("abc")
    intercept[InvalidValue[?, ?]](read[Code]("code-bad", "varchar(8)", "'abcd'")).getMessage should include("not three letters")
  }

  test("a rejecting apply is an InvalidValue") {
    intercept[InvalidValue[?, ?]](read[Positive]("positive", "int", "0")).getMessage should include("not positive")
  }

  test("an InstanceConverter's decodeEither decides") {
    implicit val converter: InstanceConverter[Span, String] = new InstanceConverter[Span, String] {
      def encode(obj: Span): String                                  = s"${obj.start}-${obj.end}"
      def decode(value: String): Span                                = Span(0, 0)
      override def decodeEither(value: String): Either[String, Span] = Left("no spans")
    }
    intercept[InvalidValue[?, ?]](read[Span]("span", "varchar(8)", "'1-2'")).getMessage should include("no spans")
  }
}
