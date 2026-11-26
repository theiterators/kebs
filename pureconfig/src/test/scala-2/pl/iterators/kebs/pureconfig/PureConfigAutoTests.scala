package pl.iterators.kebs.pureconfig

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.enumeratum.{KebsEnumeratum, KebsValueEnumeratum}
import pl.iterators.kebs.instances.UtilInstances
import pl.iterators.kebs.testkit.model.{Color, Id, Level}
import pureconfig.ConfigSource
import pureconfig.generic.auto._

import java.util.Currency

object PureConfigAutoTestDomain {
  final case class Settings(id: Id, color: Color, level: Level, currency: Currency, others: List[Color], nested: Nested)
  final case class Nested(level: Option[Level], byName: Map[String, Color])
}

class PureConfigAutoTests extends AnyFunSuite with Matchers {
  import PureConfigAutoTestDomain._

  object Protocol extends KebsPureConfig with KebsEnumeratum with KebsValueEnumeratum with UtilInstances with CaseClass1ToValueClass
  import Protocol._

  test("next to pureconfig's automatic derivation") {
    ConfigSource
      .string("id = x, color = red, level = 2, currency = USD, others = [red], nested { level = 1, by-name { a = darkBlue } }")
      .load[Settings] shouldBe Right(
      Settings(
        Id("x"),
        Color.Red,
        Level.High,
        Currency.getInstance("USD"),
        List(Color.Red),
        Nested(Some(Level.Low), Map("a" -> Color.DarkBlue))
      )
    )
  }
}
