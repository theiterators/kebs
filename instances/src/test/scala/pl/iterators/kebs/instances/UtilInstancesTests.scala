package pl.iterators.kebs.instances

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.instances.UtilInstances.{CurrencyString, LocaleString, UUIDString}
import pl.iterators.kebs.json.KebsSpray
import spray.json._

import java.util.{Currency, Locale, UUID}

class UtilInstancesTests
    extends AnyFunSuite
    with Matchers
    with DefaultJsonProtocol
    with KebsSpray
    with UtilInstances
    with CurrencyString
    with LocaleString
    with UUIDString {

  test("Currency standard format") {
    val jf    = implicitly[JsonFormat[Currency]]
    val value = "PLN"
    val obj   = Currency.getInstance(value)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("Currency wrong format exception") {
    import UtilInstances.CurrencyFormat

    val jf    = implicitly[JsonFormat[Currency]]
    val value = "not a Currency"

    val thrown = intercept[IllegalArgumentException] {
      jf.read(JsString(value))
    }
    assert(thrown.getMessage === errorMessage[Currency, String](classOf[Currency], value, CurrencyFormat))
  }

  test("Locale standard format") {
    val jf    = implicitly[JsonFormat[Locale]]
    val value = "pl-PL"
    val obj   = Locale.forLanguageTag(value)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("UUID standard format") {
    val jf    = implicitly[JsonFormat[UUID]]
    val value = "123e4567-e89b-12d3-a456-426614174000"
    val obj   = UUID.fromString(value)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("UUID wrong format exception") {
    import UtilInstances.UUIDFormat

    val jf    = implicitly[JsonFormat[UUID]]
    val value = "not an UUID"

    val thrown = intercept[IllegalArgumentException] {
      jf.read(JsString(value))
    }
    assert(thrown.getMessage === errorMessage[UUID, String](classOf[UUID], value, UUIDFormat))
  }
}
