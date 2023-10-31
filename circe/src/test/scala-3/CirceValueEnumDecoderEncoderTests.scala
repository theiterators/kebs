import enumeratum.values.{LongEnum, LongEnumEntry}
import io.circe._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.circe.KebsEnumFormats
import pl.iterators.kebs.enums.KebsValueEnum

class CirceValueEnumDecoderEncoderTests extends AnyFunSuite with Matchers with KebsValueEnum {

  object KebsProtocol extends KebsEnumFormats

  enum LongGreeting(val value: Long) {
    case  Hello   extends LongGreeting(0L)
    case  GoodBye extends LongGreeting(1L)
    case  Hi      extends LongGreeting(2L)
    case  Bye     extends LongGreeting(3L) 
  }

  import LongGreeting._

  test("value enum JsonFormat") {
    import KebsProtocol._
    val decoder = implicitly[Decoder[LongGreeting]]
    val encoder = implicitly[Encoder[LongGreeting]]

    decoder(Json.fromLong(0L).hcursor) shouldBe Right(Hello)
    decoder(Json.fromLong(1L).hcursor) shouldBe Right(GoodBye)

    encoder(Hello) shouldBe Json.fromLong(0L)
    encoder(GoodBye) shouldBe Json.fromLong(1L)
  }

  test("value enum deserialization error") {
    import KebsProtocol._
    val decoder = implicitly[Decoder[LongGreeting]]
    decoder(Json.fromLong(4L).hcursor) shouldBe Left(DecodingFailure("4 is not a member of 0, 1, 2, 3", List.empty[CursorOp]))
  }
}
