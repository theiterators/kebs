import enumeratum.{Enum, EnumEntry}
import io.circe._
import org.scalatest.matchers.should.Matchers
import org.scalatest.funsuite.AnyFunSuite
import pl.iterators.kebs.circe.KebsEnumFormats

class CirceEnumDecoderEncoderTests extends AnyFunSuite with Matchers {
  sealed trait Greeting extends EnumEntry

  object Greeting extends Enum[Greeting] {
    val values = findValues

    case object Hello   extends Greeting
    case object GoodBye extends Greeting
    case object Hi      extends Greeting
    case object Bye     extends Greeting
  }

  import Greeting._

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
      DecodingFailure("1 should be a string of value Hello, GoodBye, Hi, Bye", List.empty[CursorOp]))
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
