package instances

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.converters.InstanceConverter.DecodeErrorException
import pl.iterators.kebs.instances.UtilInstances
import pl.iterators.kebs.json.KebsSpray
import spray.json._

import java.util.{Currency, Locale, UUID}

class UtilInstancesTests extends AnyFunSuite with Matchers with DefaultJsonProtocol with KebsSpray with UtilInstances {

  test("No CaseClass1Rep implicits derived") {
    import pl.iterators.kebs.macros.CaseClass1Rep

    "implicitly[CaseClass1Rep[Currency, String]]" shouldNot typeCheck
    "implicitly[CaseClass1Rep[String, Currency]]" shouldNot typeCheck
    "implicitly[CaseClass1Rep[Locale, String]]" shouldNot typeCheck
    "implicitly[CaseClass1Rep[String, Locale]]" shouldNot typeCheck
    "implicitly[CaseClass1Rep[UUID, String]]" shouldNot typeCheck
    "implicitly[CaseClass1Rep[String, UUID]]" shouldNot typeCheck
  }

  test("Currency standard format") {
    val jf    = implicitly[JsonFormat[Currency]]
    val value = "PLN"
    val obj   = Currency.getInstance(value)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("Currency wrong format exception") {
    val jf    = implicitly[JsonFormat[Currency]]
    val value = "not a Currency"

    assertThrows[DecodeErrorException](jf.read(JsString(value)))
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
    val jf    = implicitly[JsonFormat[UUID]]
    val value = "not an UUID"

    assertThrows[DecodeErrorException](jf.read(JsString(value)))
  }
}
