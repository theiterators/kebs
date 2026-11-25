package pl.iterators.kebs.jsoniter.formats

import com.github.plokhotnyuk.jsoniter_scala.core._
import org.scalatest.Assertion
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

abstract class JsoniterTestBase extends AnyFunSuite with Matchers {
  protected def roundTrip[T](value: T, json: String)(implicit codec: JsonValueCodec[T]): Assertion = {
    writeToString(value) shouldBe json
    readFromString[T](json) shouldBe value
  }

  protected def rejected[T](json: String, mentioning: String)(implicit codec: JsonValueCodec[T]): Assertion =
    (the[JsonReaderException] thrownBy readFromString[T](json)).getMessage should include(mentioning)
}
