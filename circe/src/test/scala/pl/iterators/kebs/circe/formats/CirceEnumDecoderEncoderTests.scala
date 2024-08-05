package pl.iterators.kebs.circe.formats

import io.circe._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.circe.KebsEnumForTests
import pl.iterators.kebs.circe.enums.KebsCirceEnum
import pl.iterators.kebs.circe.enums.{KebsCirceEnumLowercase, KebsCirceEnumUppercase}
import pl.iterators.kebs.circe.model.Greeting._
import pl.iterators.kebs.circe.model._

class CirceEnumDecoderEncoderTests extends AnyFunSuite with Matchers with KebsEnumForTests {

  object KebsProtocol          extends KebsCirceEnum
  object KebsProtocolUppercase extends KebsCirceEnumUppercase
  object KebsProtocolLowercase extends KebsCirceEnumLowercase

  test("enum JsonFormat") {
    import KebsProtocol._
    val decoder = implicitly[Decoder[Greeting]]
    val encoder = implicitly[Encoder[Greeting]]
    decoder(Json.fromString("hElLo").hcursor) shouldBe Right(Hello)
    decoder(Json.fromString("goodbye").hcursor) shouldBe Right(GoodBye)
    encoder(Hello) shouldBe Json.fromString("Hello")
    encoder(GoodBye) shouldBe Json.fromString("GoodBye")
  }

  test("enum name deserialization error") {
    import KebsProtocol._
    val decoder = implicitly[Decoder[Greeting]]
    decoder(Json.fromInt(1).hcursor) shouldBe Left(
      DecodingFailure("1 should be a string of value Hello, GoodBye, Hi, Bye", List.empty[CursorOp])
    )
  }

  test("enum JsonFormat - lowercase") {
    import KebsProtocol._
    val decoder = implicitly[Decoder[Greeting]]
    val encoder = implicitly[Encoder[Greeting]]
    decoder(Json.fromString("hello").hcursor) shouldBe Right(Hello)
    decoder(Json.fromString("goodbye").hcursor) shouldBe Right(GoodBye)
    encoder(Hello) shouldBe Json.fromString("Hello")
    encoder(GoodBye) shouldBe Json.fromString("GoodBye")
  }

  test("enum JsonFormat - uppercase") {
    import KebsProtocol._
    val decoder = implicitly[Decoder[Greeting]]
    val encoder = implicitly[Encoder[Greeting]]
    decoder(Json.fromString("HELLO").hcursor) shouldBe Right(Hello)
    decoder(Json.fromString("GOODBYE").hcursor) shouldBe Right(GoodBye)
    encoder(Hello) shouldBe Json.fromString("Hello")
    encoder(GoodBye) shouldBe Json.fromString("GoodBye")
  }
}
