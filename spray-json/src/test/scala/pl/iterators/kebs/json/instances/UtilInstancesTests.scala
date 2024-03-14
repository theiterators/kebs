package pl.iterators.kebs.json.instances

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.instances.InstanceConverter.DecodeErrorException
import pl.iterators.kebs.instances.UtilInstances
import pl.iterators.kebs.json.KebsSpray
import spray.json._

import java.util.{Currency, Locale, UUID}

class UtilInstancesTests extends AnyFunSuite with Matchers with DefaultJsonProtocol with KebsSpray with UtilInstances {

  test("No ValueClassLike implicits derived") {
    import pl.iterators.kebs.core.macros.ValueClassLike

    "implicitly[ValueClassLike[Currency, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, Currency]]" shouldNot typeCheck
    "implicitly[ValueClassLike[Locale, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, Locale]]" shouldNot typeCheck
    "implicitly[ValueClassLike[UUID, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, UUID]]" shouldNot typeCheck
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
