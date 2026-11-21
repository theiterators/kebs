package pl.iterators.kebs.playjson

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.enums.EnumNaming
import pl.iterators.kebs.enumeratum.{KebsEnumeratum, KebsValueEnumeratum}
import pl.iterators.kebs.testkit.model._
import play.api.libs.json._

class PlayJsonEnumNamingTests extends AnyFunSuite with Matchers with KebsEnumeratum with KebsValueEnumeratum {
  test("an EnumNaming in scope renames enum values") {
    import pl.iterators.kebs.playjson.enums._
    implicit val enumNaming: EnumNaming      = EnumNaming.screamingSnakeCase
    implicit val paintFormat: OFormat[Paint] = Json.format[Paint]
    Json.toJson(Paint(Color.DarkBlue, Level.High)) shouldBe Json.parse("""{"color":"DARK_BLUE","level":2}""")
    Json.parse("""{"color":"dark_blue","level":2}""").as[Paint] shouldBe Paint(Color.DarkBlue, Level.High)
  }

  test("the uppercase preset decodes case-insensitively") {
    import pl.iterators.kebs.playjson.enums.uppercase._
    implicit val paintFormat: OFormat[Paint] = Json.format[Paint]
    Json.parse("""{"color":"darkblue","level":2}""").as[Paint] shouldBe Paint(Color.DarkBlue, Level.High)
  }
}
