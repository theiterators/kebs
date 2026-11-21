package pl.iterators.kebs.http4s

import org.http4s.{QueryParamDecoder, QueryParameterValue}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.enums.EnumNaming
import pl.iterators.kebs.enumeratum.KebsEnumeratum
import pl.iterators.kebs.testkit.model.Color

class Http4sEnumNamingTests extends AnyFunSuite with Matchers with KebsEnumeratum {
  implicit val enumNaming: EnumNaming = EnumNaming.screamingSnakeCase

  test("EnumString uses the EnumNaming in scope") {
    EnumString[Color].unapply("dark_blue") shouldBe Some(Color.DarkBlue)
  }

  test("enum query parameters use the EnumNaming in scope") {
    implicitly[QueryParamDecoder[Color]].decode(QueryParameterValue("DARK_BLUE")).toOption shouldBe Some(Color.DarkBlue)
  }
}
