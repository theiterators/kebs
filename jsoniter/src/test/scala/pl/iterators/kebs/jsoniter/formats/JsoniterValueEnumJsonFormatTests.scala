package pl.iterators.kebs.jsoniter.formats

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.enums.ValueEnumLikeEntry
import pl.iterators.kebs.enumeratum.KebsValueEnumeratum
import pl.iterators.kebs.jsoniter.KebsJsoniter
import pl.iterators.kebs.jsoniter.enums.{KebsJsoniterEnums, KebsJsoniterValueEnums}
import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros._
import pl.iterators.kebs.jsoniter.model._
import pl.iterators.kebs.jsoniter.model.LongGreeting._
import pl.iterators.kebs.jsoniter.KebsEnumForTests
import pl.iterators.kebs.core.enums.ValueEnumLike
import pl.iterators.kebs.jsoniter.KebsValueEnumForTests
import pl.iterators.kebs.jsoniter.enums.JsoniterReaderExceptionImpl

class JsoniterValueEnumDecoderEncoderTests extends AnyFunSuite with Matchers with KebsValueEnumForTests {

  object KebsProtocol extends KebsJsoniterValueEnums
  test("value enum JsonFormat") {
    import KebsProtocol._
    val codec = implicitly[JsonValueCodec[LongGreeting]]

    readFromString[LongGreeting]("0") shouldBe Hello
    readFromString[LongGreeting]("1") shouldBe GoodBye

    writeToString[LongGreeting](Hello) shouldBe "0"
    writeToString[LongGreeting](GoodBye) shouldBe "1"
  }

  test("value enum deserialization error") {
    import KebsProtocol._
    val codec = implicitly[JsonValueCodec[LongGreeting]]

    val exception = intercept[JsoniterReaderExceptionImpl] {
      readFromString[LongGreeting]("4")
    }
    exception.getMessage() should include("4 is not a member of 0, 1, 2, 3")
  }
}
