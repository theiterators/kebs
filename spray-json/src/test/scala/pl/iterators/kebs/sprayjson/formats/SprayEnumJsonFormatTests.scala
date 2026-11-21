package pl.iterators.kebs.sprayjson.formats

import enumeratum.{Enum, EnumEntry}
import pl.iterators.kebs.sprayjson.{KebsSprayJson, KebsSprayJsonEnums}
import spray.json._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.enumeratum.KebsEnumeratum
import pl.iterators.kebs.sprayjson.enums.{KebsSprayJsonEnumsLowercase, KebsSprayJsonEnumsUppercase}

class SprayEnumJsonFormatTests extends AnyFunSuite with Matchers with KebsEnumeratum {
  sealed trait Greeting extends EnumEntry

  object Greeting extends Enum[Greeting] {
    val values = findValues

    case object Hello   extends Greeting
    case object GoodBye extends Greeting
    case object Hi      extends Greeting
    case object Bye     extends Greeting
  }

  import Greeting._

  sealed trait Status extends EnumEntry

  object Status extends Enum[Status] {
    val values = findValues

    case object Active   extends Status { override val entryName = "is-active" }
    case object Inactive extends Status
  }

  object KebsProtocol          extends DefaultJsonProtocol with KebsSprayJson with KebsSprayJsonEnums
  object KebsProtocolUppercase extends DefaultJsonProtocol with KebsSprayJson with KebsSprayJsonEnumsUppercase
  object KebsProtocolLowercase extends DefaultJsonProtocol with KebsSprayJson with KebsSprayJsonEnumsLowercase

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

  test("enum JsonFormat uses entryName") {
    import KebsProtocol._
    val jf = implicitly[JsonFormat[Status]]
    jf.write(Status.Active) shouldBe JsString("is-active")
    jf.read(JsString("IS-ACTIVE")) shouldBe Status.Active
    the[DeserializationException] thrownBy jf.read(JsString("Active")) should have message "Active should be one of is-active, Inactive"
  }

  test("enum JsonFormat uses entryName - lowercase") {
    import KebsProtocolLowercase._
    val jf = implicitly[JsonFormat[Status]]
    jf.write(Status.Active) shouldBe JsString("is-active")
    jf.read(JsString("is-active")) shouldBe Status.Active
  }

  test("enum JsonFormat uses entryName - uppercase") {
    import KebsProtocolUppercase._
    val jf = implicitly[JsonFormat[Status]]
    jf.write(Status.Active) shouldBe JsString("IS-ACTIVE")
    jf.read(JsString("IS-ACTIVE")) shouldBe Status.Active
  }
}
