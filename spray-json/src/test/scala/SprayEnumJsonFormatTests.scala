import org.scalatest.{FunSuite, Matchers}
import enumeratum.{Enum, EnumEntry}
import pl.iterators.kebs.json.{KebsEnumFormats, KebsSpray}
import spray.json._

class SprayEnumJsonFormatTests extends FunSuite with Matchers {
  sealed trait Greeting extends EnumEntry

  object Greeting extends Enum[Greeting] {
    val values = findValues

    case object Hello   extends Greeting
    case object GoodBye extends Greeting
    case object Hi      extends Greeting
    case object Bye     extends Greeting
  }

  import Greeting._

  object KebsProtocol          extends DefaultJsonProtocol with KebsSpray with KebsEnumFormats
  object KebsProtocolUppercase extends DefaultJsonProtocol with KebsSpray with KebsEnumFormats.Uppercase
  object KebsProtocolLowercase extends DefaultJsonProtocol with KebsSpray with KebsEnumFormats.Lowercase

  test("enum JsonFormat") {
    import KebsProtocol._
    val jf = implicitly[JsonFormat[Greeting]]
    jf.read(JsString("hello")) shouldBe Hello
    jf.read(JsString("goodbye")) shouldBe GoodBye
    jf.write(Hello) shouldBe JsString("Hello")
    jf.write(GoodBye) shouldBe JsString("GoodBye")
  }

  test("enum name deserialization error") {
    import KebsProtocol._
    val jf = implicitly[JsonFormat[Greeting]]
    the[DeserializationException] thrownBy jf.read(JsString("xxx")) should have message "xxx should be one of Hello, GoodBye, Hi, Bye"
  }

  test("enum value deserialization error") {
    import KebsProtocol._
    val jf = implicitly[JsonFormat[Greeting]]
    the[DeserializationException] thrownBy jf.read(JsTrue) should have message "true should be a string of value Hello, GoodBye, Hi, Bye"
  }

  test("enum JsonFormat - lowercase") {
    import KebsProtocolLowercase._
    val jf = implicitly[JsonFormat[Greeting]]
    jf.read(JsString("hello")) shouldBe Hello
    jf.read(JsString("goodbye")) shouldBe GoodBye
    jf.write(Hello) shouldBe JsString("hello")
    jf.write(GoodBye) shouldBe JsString("goodbye")
  }

  test("enum JsonFormat - uppercase") {
    import KebsProtocolUppercase._
    val jf = implicitly[JsonFormat[Greeting]]
    jf.read(JsString("HELLO")) shouldBe Hello
    jf.read(JsString("GOODBYE")) shouldBe GoodBye
    jf.write(Hello) shouldBe JsString("HELLO")
    jf.write(GoodBye) shouldBe JsString("GOODBYE")
  }
}
