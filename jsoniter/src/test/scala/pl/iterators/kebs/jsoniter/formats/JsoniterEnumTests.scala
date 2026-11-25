package pl.iterators.kebs.jsoniter.formats

import com.github.plokhotnyuk.jsoniter_scala.core._
import pl.iterators.kebs.core.enums.EnumNaming
import pl.iterators.kebs.enumeratum.{KebsEnumeratum, KebsValueEnumeratum}
import pl.iterators.kebs.jsoniter.KebsJsoniter
import pl.iterators.kebs.jsoniter.enums.{KebsJsoniterEnums, KebsJsoniterEnumsLowercase, KebsJsoniterEnumsUppercase, KebsJsoniterValueEnums}
import pl.iterators.kebs.testkit.model._

class JsoniterEnumTests extends JsoniterTestBase {
  object Protocol extends KebsJsoniter with KebsEnumeratum with KebsValueEnumeratum with KebsJsoniterEnums with KebsJsoniterValueEnums
  object Uppercase
      extends KebsJsoniter
      with KebsEnumeratum
      with KebsValueEnumeratum
      with KebsJsoniterEnumsUppercase
      with KebsJsoniterValueEnums
  object Lowercase extends KebsJsoniter with KebsEnumeratum with KebsJsoniterEnumsLowercase

  test("enums") {
    import Protocol._
    roundTrip[Color](Color.DarkBlue, "\"DarkBlue\"")
    readFromString[Color]("\"darkblue\"") shouldBe Color.DarkBlue
    rejected[Color]("\"Purple\"", "Purple should be one of Red, DarkBlue")
    rejected[Color]("null", "")
    rejected[Color]("1", "")
  }

  test("value enums") {
    import Protocol._
    roundTrip[Level](Level.High, "2")
    rejected[Level]("7", "7 is not a member of")
    rejected[Level]("\"2\"", "")
  }

  test("as fields and in containers") {
    import Protocol._
    roundTrip(Paint(Color.DarkBlue, Level.High), """{"color":"DarkBlue","level":2}""")
    roundTrip[Map[String, Color]](Map("a" -> Color.Red), """{"a":"Red"}""")
  }

  test("presets") {
    {
      import Uppercase._
      roundTrip(Paint(Color.DarkBlue, Level.High), """{"color":"DARKBLUE","level":2}""")
      readFromString[Paint]("""{"color":"darkblue","level":2}""") shouldBe Paint(Color.DarkBlue, Level.High)
    }

    {
      import Lowercase._
      roundTrip[Color](Color.DarkBlue, "\"darkblue\"")
    }
  }

  test("an EnumNaming in scope") {
    import Protocol._
    implicit val naming: EnumNaming = EnumNaming.screamingSnakeCase
    writeToString(Paint(Color.DarkBlue, Level.High)) shouldBe """{"color":"DARK_BLUE","level":2}"""
    readFromString[Paint]("""{"color":"dark_blue","level":2}""") shouldBe Paint(Color.DarkBlue, Level.High)
  }
}
