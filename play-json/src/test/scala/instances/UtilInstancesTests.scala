package instances

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.instances.InstanceConverter.DecodeErrorException
import pl.iterators.kebs.instances.UtilInstances
import play.api.libs.json.{Format, JsString, JsSuccess}

import java.util.{Currency, Locale, UUID}

class UtilInstancesTests extends AnyFunSuite with Matchers with UtilInstances {
  import pl.iterators.kebs.json._
  test("No ValueClassLike implicits derived") {

    "implicitly[ValueClassLike[Currency, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, Currency]]" shouldNot typeCheck
    "implicitly[ValueClassLike[Locale, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, Locale]]" shouldNot typeCheck
    "implicitly[ValueClassLike[UUID, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, UUID]]" shouldNot typeCheck
  }

  test("Currency standard format") {
    val jf    = implicitly[Format[Currency]]
    val value = "PLN"
    val obj   = Currency.getInstance(value)

    jf.writes(obj) shouldBe JsString(value)
    jf.reads(JsString(value)) shouldBe JsSuccess(obj)
  }

  test("Currency wrong format exception") {
    val jf    = implicitly[Format[Currency]]
    val value = "not a Currency"

    assertThrows[DecodeErrorException](jf.reads(JsString(value)))
  }

  test("Locale standard format") {
    val jf    = implicitly[Format[Locale]]
    val value = "pl-PL"
    val obj   = Locale.forLanguageTag(value)

    jf.writes(obj) shouldBe JsString(value)
    jf.reads(JsString(value)) shouldBe JsSuccess(obj)
  }

  test("UUID standard format") {
    val jf    = implicitly[Format[UUID]]
    val value = "123e4567-e89b-12d3-a456-426614174000"
    val obj   = UUID.fromString(value)

    jf.writes(obj) shouldBe JsString(value)
    jf.reads(JsString(value)) shouldBe JsSuccess(obj)
  }

  test("UUID wrong format exception") {
    val jf    = implicitly[Format[UUID]]
    val value = "not an UUID"

    assertThrows[DecodeErrorException](jf.reads(JsString(value)))
  }
}
