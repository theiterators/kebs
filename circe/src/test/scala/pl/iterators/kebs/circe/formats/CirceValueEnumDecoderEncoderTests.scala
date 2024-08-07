package pl.iterators.kebs.circe

import io.circe._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.circe.enums.KebsCirceValueEnums
import pl.iterators.kebs.circe.model.LongGreeting
import pl.iterators.kebs.circe.model.LongGreeting._

class CirceValueEnumDecoderEncoderTests extends AnyFunSuite with Matchers with KebsValueEnumForTests {

  object KebsProtocol extends KebsCirceValueEnums

  test("value enum JsonFormat") {
    import KebsProtocol._
    val decoder = implicitly[Decoder[LongGreeting]]
    val encoder = implicitly[Encoder[LongGreeting]]
    decoder(Json.fromLong(0).hcursor) shouldBe Right(Hello)
    decoder(Json.fromLong(1).hcursor) shouldBe Right(GoodBye)
    encoder(Hello) shouldBe Json.fromLong(0)
    encoder(GoodBye) shouldBe Json.fromLong(1)
  }

  test("value enum deserialization error") {
    import KebsProtocol._
    val decoder = implicitly[Decoder[LongGreeting]]
    decoder(Json.fromLong(4).hcursor) shouldBe Left(DecodingFailure("4 is not a member of 0, 1, 2, 3", List.empty[CursorOp]))
  }
}
