import enumeratum.values.{LongEnum, LongEnumEntry}
import org.scalatest.{FunSuite, Matchers}
import pl.iterators.kebs.json.{KebsEnumFormats, KebsSpray}
import spray.json._

class SprayValueEnumJsonFormatTests extends FunSuite with Matchers {
  sealed abstract class LongGreeting(val value: Long) extends LongEnumEntry

  object LongGreeting extends LongEnum[LongGreeting] {
    val values = findValues

    case object Hello   extends LongGreeting(0L)
    case object GoodBye extends LongGreeting(1L)
    case object Hi      extends LongGreeting(2L)
    case object Bye     extends LongGreeting(3L)
  }

  import LongGreeting._

  object KebsProtocol extends DefaultJsonProtocol with KebsSpray with KebsEnumFormats

  test("value enum JsonFormat") {
    import KebsProtocol._
    val jf = implicitly[JsonFormat[LongGreeting]]
    jf.read(JsNumber(0)) shouldBe Hello
    jf.read(JsNumber(1)) shouldBe GoodBye
    jf.write(Hello) shouldBe JsNumber(0)
    jf.write(GoodBye) shouldBe JsNumber(1)
  }

  test("value enum deserialization error") {
    import KebsProtocol._
    val jf = implicitly[JsonFormat[LongGreeting]]
    the[DeserializationException] thrownBy jf.read(JsNumber(4)) should have message "4 is not a member of 0, 1, 2, 3"
  }
}
