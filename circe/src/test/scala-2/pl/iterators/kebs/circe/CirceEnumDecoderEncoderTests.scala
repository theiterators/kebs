package pl.iterators.kebs.circe

import io.circe._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.enumeratum.KebsEnumeratum
import pl.iterators.kebs.circe.model._
import pl.iterators.kebs.circe.model.Greeting._

class CirceEnumDecoderEncoderTests extends AnyFunSuite with Matchers with KebsEnumeratum {

  object KebsProtocol          extends KebsEnumFormats
  object KebsProtocolUppercase extends KebsEnumFormats.Uppercase
  object KebsProtocolLowercase extends KebsEnumFormats.Lowercase

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
