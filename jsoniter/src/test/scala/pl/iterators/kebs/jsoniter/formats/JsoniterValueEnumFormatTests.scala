package pl.iterators.kebs.jsoniter.formats

import com.github.plokhotnyuk.jsoniter_scala.core._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.jsoniter.{KebsJsoniter, KebsValueEnumForTests}
import pl.iterators.kebs.jsoniter.enums.KebsJsoniterValueEnums
import pl.iterators.kebs.jsoniter.model.LongGreeting
import pl.iterators.kebs.jsoniter.model.LongGreeting._

class JsoniterValueEnumFormatTests extends AnyFunSuite with Matchers with KebsValueEnumForTests {

  object KebsProtocol extends KebsJsoniter with KebsJsoniterValueEnums

  test("value enum codec - read") {
    import KebsProtocol._
    val codec = implicitly[JsonValueCodec[LongGreeting]]
    readFromString[LongGreeting]("0")(codec) shouldBe Hello
    readFromString[LongGreeting]("1")(codec) shouldBe GoodBye
  }

  test("value enum codec - write") {
    import KebsProtocol._
    val codec = implicitly[JsonValueCodec[LongGreeting]]
    writeToString[LongGreeting](Hello)(codec) shouldBe "0"
    writeToString[LongGreeting](GoodBye)(codec) shouldBe "1"
  }

  test("value enum codec - deserialization error") {
    import KebsProtocol._
    val codec = implicitly[JsonValueCodec[LongGreeting]]
    intercept[JsonReaderException] {
      readFromString[LongGreeting]("4")(codec)
    }.getMessage should include("4 is not a member of")
  }
}
