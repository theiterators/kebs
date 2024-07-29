package pl.iterators.kebs.sprayjson

import enumeratum.values.{LongEnum, LongEnumEntry}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.enums.ValueEnumLikeEntry
import pl.iterators.kebs.enumeratum.KebsValueEnumeratum
import spray.json._

class SprayValueEnumJsonFormatTests extends AnyFunSuite with Matchers with KebsValueEnumeratum {
  sealed abstract class LongGreeting(val value: Long) extends LongEnumEntry with ValueEnumLikeEntry[Long]

  object LongGreeting extends LongEnum[LongGreeting] {
    val values = findValues

    case object Hello   extends LongGreeting(0L)
    case object GoodBye extends LongGreeting(1L)
    case object Hi      extends LongGreeting(2L)
    case object Bye     extends LongGreeting(3L)
  }

  import LongGreeting._

  object KebsProtocol extends DefaultJsonProtocol with KebsSprayJson with KebsEnumFormats

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
