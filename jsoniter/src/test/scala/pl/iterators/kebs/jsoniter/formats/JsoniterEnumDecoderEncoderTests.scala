package pl.iterators.kebs.jsoniter.formats

import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.jsoniter.KebsEnumForTests
import pl.iterators.kebs.jsoniter.enums.KebsJsoniterEnums
import pl.iterators.kebs.jsoniter.enums.KebsJsoniterEnumsLowercase
import pl.iterators.kebs.jsoniter.enums.KebsJsoniterEnumsUppercase
import pl.iterators.kebs.jsoniter.model.Greeting._
import pl.iterators.kebs.jsoniter.model._
import pl.iterators.kebs.jsoniter.enums.JsoniterReaderExceptionImpl

class JsoniterEnumCodecTests extends AnyFunSuite with Matchers with KebsEnumForTests {

  object KebsProtocol          extends KebsJsoniterEnums
  object KebsProtocolLowercase extends KebsJsoniterEnumsLowercase
  object KebsProtocolUppercase extends KebsJsoniterEnumsUppercase

  test("enum JsonValueCodec") {
    import KebsProtocol._
    val codec = implicitly[JsonValueCodec[Greeting]]

    readFromString[Greeting]("\"hElLo\"")(codec) shouldBe Hello
    readFromString[Greeting]("\"goodbye\"")(codec) shouldBe GoodBye
    writeToString(Hello)(codec) shouldBe "\"Hello\""
    writeToString(GoodBye)(codec) shouldBe "\"GoodBye\""
  }

  test("enum name deserialization error") {
    import KebsProtocol._
    val codec = implicitly[JsonValueCodec[Greeting]]

    val exception = intercept[JsonReaderException] {
      readFromString[Greeting]("1")(codec)
    }
    exception.getMessage should include("expected '\"', offset: 0x00000000")
  }

  test("enum value deserialization error") {
    import KebsProtocol._
    val codec = implicitly[JsonValueCodec[Greeting]]

    val exception = intercept[JsoniterReaderExceptionImpl] {
      readFromString[Greeting]("\"invalid hello\"")(codec)
    }
    exception.getMessage should include("is not a member of enum values: Hello, GoodBye, Hi, Bye")
  }

  test("enum JsonValueCodec - lowercase") {
    import KebsProtocolLowercase._
    val codec = implicitly[JsonValueCodec[Greeting]]

    readFromString[Greeting]("\"hello\"")(codec) shouldBe Hello
    readFromString[Greeting]("\"goodbye\"")(codec) shouldBe GoodBye
    writeToString(Hello)(codec) shouldBe "\"hello\""
    writeToString(GoodBye)(codec) shouldBe "\"goodbye\""
  }

  test("enum JsonValueCodec - uppercase") {
    import KebsProtocolUppercase._
    val codec = implicitly[JsonValueCodec[Greeting]]

    readFromString[Greeting]("\"HELLO\"")(codec) shouldBe Hello
    readFromString[Greeting]("\"GOODBYE\"")(codec) shouldBe GoodBye
    writeToString(Hello)(codec) shouldBe "\"HELLO\""
    writeToString(GoodBye)(codec) shouldBe "\"GOODBYE\""
  }
}
