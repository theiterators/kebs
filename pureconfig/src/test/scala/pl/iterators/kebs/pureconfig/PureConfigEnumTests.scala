package pl.iterators.kebs.pureconfig

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.enums.EnumNaming
import pl.iterators.kebs.enumeratum.{KebsEnumeratum, KebsValueEnumeratum}
import pl.iterators.kebs.testkit.model.{Color, Level, Paint}
import pureconfig.error.{CannotConvert, ConvertFailure}
import pureconfig.generic.semiauto._
import pureconfig.{ConfigReader, ConfigSource, ConfigWriter}

class PureConfigEnumTests extends AnyFunSuite with Matchers {
  object Protocol extends KebsPureConfig with KebsEnumeratum with KebsValueEnumeratum
  import Protocol._

  private def read[T: ConfigReader](value: String)           = ConfigSource.string(s"key = $value").at("key").load[T]
  private def written[T: ConfigWriter](value: T): Any        = ConfigWriter[T].to(value).unwrapped
  private def reason[T: ConfigReader](value: String): String = read[T](value).left.toOption.map(_.head) match {
    case Some(ConvertFailure(CannotConvert(_, _, because), _, _)) => because
    case other                                                    => other.toString
  }

  test("enums") {
    read[Color]("DarkBlue") shouldBe Right(Color.DarkBlue)
    read[Color]("darkblue") shouldBe Right(Color.DarkBlue)
    written[Color](Color.DarkBlue) shouldBe "DarkBlue"
  }

  test("enums with an EnumNaming in scope") {
    implicit val naming: EnumNaming = EnumNaming.screamingSnakeCase
    read[Color]("dark_blue") shouldBe Right(Color.DarkBlue)
    written[Color](Color.DarkBlue) shouldBe "DARK_BLUE"
  }

  test("value enums") {
    read[Level]("2") shouldBe Right(Level.High)
    written[Level](Level.High) shouldBe 2
  }

  test("what is not a value of the enum") {
    reason[Color]("purple") shouldBe "expected one of: Red, DarkBlue"
    reason[Level]("9") shouldBe "expected one of: 1, 2"
    read[Level]("x").isLeft shouldBe true
    read[Color]("{}").isLeft shouldBe true
  }

  test("as fields and in containers") {
    implicit val reader: ConfigReader[Paint] = deriveReader[Paint]
    implicit val writer: ConfigWriter[Paint] = deriveWriter[Paint]
    read[Paint]("{ color = red, level = 1 }") shouldBe Right(Paint(Color.Red, Level.Low))
    ConfigWriter[Paint].to(Paint(Color.Red, Level.Low)) shouldBe ConfigSource.string("color = Red, level = 1").value().toOption.get
    read[List[Color]]("[red, darkBlue]") shouldBe Right(List(Color.Red, Color.DarkBlue))
    read[Option[Level]]("1") shouldBe Right(Some(Level.Low))
  }
}
