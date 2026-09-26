package pl.iterators.kebs.circe.formats

import enumeratum.{Enum, EnumEntry}
import io.circe._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.circe.enums.{KebsCirceEnums, KebsCirceEnumsLowercase, KebsCirceEnumsUppercase}
import pl.iterators.kebs.enumeratum.KebsEnumeratum

object CirceEnumEntryNameTests {
  sealed trait Status extends EnumEntry
  object Status       extends Enum[Status] {
    case object Active   extends Status { override val entryName = "is-active" }
    case object Inactive extends Status
    val values = findValues
  }
}

class CirceEnumEntryNameTests extends AnyFunSuite with Matchers with KebsEnumeratum {
  import CirceEnumEntryNameTests._

  object KebsProtocol          extends KebsCirceEnums
  object KebsProtocolUppercase extends KebsCirceEnumsUppercase
  object KebsProtocolLowercase extends KebsCirceEnumsLowercase

  test("enum Encoder / Decoder use entryName") {
    import KebsProtocol._
    implicitly[Encoder[Status]].apply(Status.Active) shouldBe Json.fromString("is-active")
    implicitly[Decoder[Status]].decodeJson(Json.fromString("IS-ACTIVE")) shouldBe Right(Status.Active)
  }

  test("enum Encoder / Decoder use entryName - uppercase") {
    import KebsProtocolUppercase._
    implicitly[Encoder[Status]].apply(Status.Active) shouldBe Json.fromString("IS-ACTIVE")
    implicitly[Decoder[Status]].decodeJson(Json.fromString("IS-ACTIVE")) shouldBe Right(Status.Active)
  }

  test("enum Encoder / Decoder use entryName - lowercase") {
    import KebsProtocolLowercase._
    implicitly[Encoder[Status]].apply(Status.Active) shouldBe Json.fromString("is-active")
    implicitly[Decoder[Status]].decodeJson(Json.fromString("is-active")) shouldBe Right(Status.Active)
  }
}
