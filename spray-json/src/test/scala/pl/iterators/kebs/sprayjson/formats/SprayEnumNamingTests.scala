package pl.iterators.kebs.sprayjson.formats

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.enums.EnumNaming
import pl.iterators.kebs.enumeratum.{KebsEnumeratum, KebsValueEnumeratum}
import pl.iterators.kebs.sprayjson.{KebsSprayJson, KebsSprayJsonEnums, KebsSprayJsonValueEnums}
import pl.iterators.kebs.sprayjson.enums.KebsSprayJsonEnumsUppercase
import pl.iterators.kebs.testkit.model._
import spray.json._

class SprayEnumNamingTests extends AnyFunSuite with Matchers {
  object Protocol
      extends DefaultJsonProtocol
      with KebsSprayJson
      with KebsEnumeratum
      with KebsValueEnumeratum
      with KebsSprayJsonEnums
      with KebsSprayJsonValueEnums
  object Uppercase
      extends DefaultJsonProtocol
      with KebsSprayJson
      with KebsEnumeratum
      with KebsValueEnumeratum
      with KebsSprayJsonEnumsUppercase
      with KebsSprayJsonValueEnums

  test("an EnumNaming in scope renames enum values") {
    import Protocol._
    implicit val enumNaming: EnumNaming = EnumNaming.screamingSnakeCase
    val format                          = implicitly[JsonFormat[Paint]]
    format.write(Paint(Color.DarkBlue, Level.High)) shouldBe """{"color":"DARK_BLUE","level":2}""".parseJson
    format.read("""{"color":"dark_blue","level":2}""".parseJson) shouldBe Paint(Color.DarkBlue, Level.High)
  }

  test("the uppercase preset decodes case-insensitively") {
    import Uppercase._
    implicitly[JsonFormat[Paint]].read("""{"color":"darkblue","level":2}""".parseJson) shouldBe Paint(Color.DarkBlue, Level.High)
  }
}
