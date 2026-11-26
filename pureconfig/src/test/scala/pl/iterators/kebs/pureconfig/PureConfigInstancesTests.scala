package pl.iterators.kebs.pureconfig

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.macros.{CaseClass1ToValueClass, ValueClassLike}
import pl.iterators.kebs.instances.time.mixins.InstantEpochMilliLong
import pl.iterators.kebs.instances.{NetInstances, TimeInstances, UtilInstances}
import pureconfig.generic.semiauto._
import pureconfig.{ConfigReader, ConfigSource, ConfigWriter}

import java.time.{Instant, YearMonth}
import java.util.{Currency, UUID}

object PureConfigInstancesTestDomain {
  final case class Positive(value: Int) { require(value > 0, "not positive") }
  final case class Code(value: String)
  object Code {
    implicit val valueClassLike: ValueClassLike[Code, String] =
      new ValueClassLike[Code, String](Code(_), _.value, s => if (s.length == 3) Right(Code(s)) else Left("not three letters"))
  }
  final case class Price(currency: Currency, since: YearMonth, amount: Positive)
}

class PureConfigInstancesTests extends AnyFunSuite with Matchers {
  import PureConfigInstancesTestDomain._

  object Protocol extends KebsPureConfig with TimeInstances with UtilInstances with NetInstances with CaseClass1ToValueClass
  object Milli    extends KebsPureConfig with InstantEpochMilliLong
  object Plain    extends KebsPureConfig

  private def read[T: ConfigReader](value: String)    = ConfigSource.string(s"key = $value").at("key").load[T]
  private def written[T: ConfigWriter](value: T): Any = ConfigWriter[T].to(value).unwrapped

  test("types with an InstanceConverter") {
    import Protocol._
    read[Currency]("USD") shouldBe Right(Currency.getInstance("USD"))
    written(Currency.getInstance("USD")) shouldBe "USD"
    read[Currency]("nope").isLeft shouldBe true
    read[YearMonth]("\"2026-01\"") shouldBe Right(YearMonth.of(2026, 1))
  }

  test("types that pureconfig reads too") {
    val id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
    val at = Instant.parse("2026-01-02T03:04:05Z")

    {
      import Protocol._
      read[UUID](s""""$id"""") shouldBe Right(id)
      written(id) shouldBe id.toString
      read[Instant](s""""$at"""") shouldBe Right(at)
    }

    {
      import Milli._
      read[Instant](at.toEpochMilli.toString) shouldBe Right(at)
      written(at) shouldBe at.toEpochMilli
    }
  }

  test("as fields") {
    import Protocol._
    implicit val reader: ConfigReader[Price] = deriveReader[Price]
    implicit val writer: ConfigWriter[Price] = deriveWriter[Price]
    val price                                = Price(Currency.getInstance("EUR"), YearMonth.of(2026, 1), Positive(3))
    read[Price]("{ currency = EUR, since = \"2026-01\", amount = 3 }") shouldBe Right(price)
    ConfigWriter[Price].to(price) shouldBe ConfigSource.string("currency = EUR, since = \"2026-01\", amount = 3").value().toOption.get
  }

  test("a value that a value class rejects is a failure, not an exception") {
    import Protocol._
    read[Positive]("0").left.toOption.get.prettyPrint() should include("not positive")
  }

  // a hand-written instance loses to CaseClass1ToValueClass in lexical scope
  test("a value class's own from decides") {
    import Plain._
    read[Code]("abc") shouldBe Right(Code("abc"))
    read[Code]("abcd").left.toOption.get.prettyPrint() should include("not three letters")
  }
}
