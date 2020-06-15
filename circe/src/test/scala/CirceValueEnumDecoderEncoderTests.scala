import enumeratum.values.{LongEnum, LongEnumEntry}
import io.circe._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.circe.KebsEnumFormats

class CirceValueEnumDecoderEncoderTests extends AnyFunSuite with Matchers {
  sealed abstract class LongGreeting(val value: Long) extends LongEnumEntry

  object LongGreeting extends LongEnum[LongGreeting] {
    val values = findValues

    case object Hello   extends LongGreeting(0L)
    case object GoodBye extends LongGreeting(1L)
    case object Hi      extends LongGreeting(2L)
    case object Bye     extends LongGreeting(3L)
  }

  import LongGreeting._

  object KebsProtocol extends KebsEnumFormats

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
