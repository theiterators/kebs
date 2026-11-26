package pl.iterators.kebs.circe.formats

import io.circe.{Decoder, Encoder, Json}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.circe.KebsCirce
import pl.iterators.kebs.circe.enums.{KebsCirceEnums, KebsCirceEnumsUppercase, KebsCirceValueEnums}
import pl.iterators.kebs.core.enums.EnumNaming
import pl.iterators.kebs.enumeratum.{KebsEnumeratum, KebsValueEnumeratum}
import pl.iterators.kebs.testkit.model._

class CirceEnumNamingTests extends AnyFunSuite with Matchers {
  private def json(s: String): Json = io.circe.parser.parse(s).fold(throw _, identity)

  object Protocol  extends KebsCirce with KebsEnumeratum with KebsValueEnumeratum with KebsCirceEnums with KebsCirceValueEnums
  object Uppercase extends KebsCirce with KebsEnumeratum with KebsValueEnumeratum with KebsCirceEnumsUppercase with KebsCirceValueEnums

  test("an EnumNaming in scope renames enum values") {
    import Protocol._
    implicit val enumNaming: EnumNaming = EnumNaming.screamingSnakeCase
    implicitly[Encoder[Paint]].apply(Paint(Color.DarkBlue, Level.High)) shouldBe json("""{"color":"DARK_BLUE","level":2}""")
    implicitly[Decoder[Paint]].decodeJson(json("""{"color":"dark_blue","level":2}""")) shouldBe Right(Paint(Color.DarkBlue, Level.High))
  }

  test("the uppercase preset decodes case-insensitively") {
    import Uppercase._
    implicitly[Decoder[Paint]].decodeJson(json("""{"color":"darkblue","level":2}""")) shouldBe Right(Paint(Color.DarkBlue, Level.High))
  }
}
