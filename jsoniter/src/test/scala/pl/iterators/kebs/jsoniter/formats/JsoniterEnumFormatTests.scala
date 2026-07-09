package pl.iterators.kebs.jsoniter.formats

import com.github.plokhotnyuk.jsoniter_scala.core._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.jsoniter.KebsEnumForTests
import pl.iterators.kebs.jsoniter.enums.{KebsJsoniterEnums, KebsJsoniterEnumsUppercase, KebsJsoniterEnumsLowercase}
import pl.iterators.kebs.jsoniter.model.Greeting
import pl.iterators.kebs.jsoniter.model.Greeting._

class JsoniterEnumFormatTests extends AnyFunSuite with Matchers with KebsEnumForTests {

  object KebsProtocol          extends KebsJsoniterEnums
  object KebsProtocolUppercase extends KebsJsoniterEnumsUppercase
  object KebsProtocolLowercase extends KebsJsoniterEnumsLowercase

  test("enum codec - case insensitive read") {
    import KebsProtocol._
    val codec = implicitly[JsonValueCodec[Greeting]]
    readFromString[Greeting]("\"hElLo\"")(codec) shouldBe Hello
    readFromString[Greeting]("\"goodbye\"")(codec) shouldBe GoodBye
  }

  test("enum codec - write") {
    import KebsProtocol._
    val codec = implicitly[JsonValueCodec[Greeting]]
    writeToString[Greeting](Hello)(codec) shouldBe "\"Hello\""
    writeToString[Greeting](GoodBye)(codec) shouldBe "\"GoodBye\""
  }

  test("enum codec - deserialization error on wrong value") {
    import KebsProtocol._
    val codec = implicitly[JsonValueCodec[Greeting]]
    intercept[JsonReaderException] {
      readFromString[Greeting]("\"NotAGreeting\"")(codec)
    }.getMessage should include("NotAGreeting should be one of")
  }

  test("enum codec - deserialization error on non-string") {
    import KebsProtocol._
    val codec = implicitly[JsonValueCodec[Greeting]]
    intercept[JsonReaderException] {
      readFromString[Greeting]("1")(codec)
    }
  }

  test("enum codec - uppercase read") {
    import KebsProtocolUppercase._
    val codec = implicitly[JsonValueCodec[Greeting]]
    readFromString[Greeting]("\"HELLO\"")(codec) shouldBe Hello
    readFromString[Greeting]("\"GOODBYE\"")(codec) shouldBe GoodBye
  }

  test("enum codec - uppercase write") {
    import KebsProtocolUppercase._
    val codec = implicitly[JsonValueCodec[Greeting]]
    writeToString[Greeting](Hello)(codec) shouldBe "\"HELLO\""
    writeToString[Greeting](GoodBye)(codec) shouldBe "\"GOODBYE\""
  }

  test("enum codec - lowercase read") {
    import KebsProtocolLowercase._
    val codec = implicitly[JsonValueCodec[Greeting]]
    readFromString[Greeting]("\"hello\"")(codec) shouldBe Hello
    readFromString[Greeting]("\"goodbye\"")(codec) shouldBe GoodBye
  }

  test("enum codec - lowercase write") {
    import KebsProtocolLowercase._
    val codec = implicitly[JsonValueCodec[Greeting]]
    writeToString[Greeting](Hello)(codec) shouldBe "\"hello\""
    writeToString[Greeting](GoodBye)(codec) shouldBe "\"goodbye\""
  }
}
