package pl.iterators.kebs.jsoniter.instances

import com.github.plokhotnyuk.jsoniter_scala.core._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.jsoniter.KebsJsoniter
import pl.iterators.kebs.instances.UtilInstances

import java.util.{Currency, Locale, UUID}

class UtilInstancesTests extends AnyFunSuite with Matchers with KebsJsoniter with UtilInstances {
  private def isScalaJS = System.getProperty("java.vm.name") == "Scala.js"
  private def isNative  = System.getProperty("java.vm.name") == "Scala Native"

  test("No ValueClassLike implicits derived") {

    "implicitly[ValueClassLike[Currency, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, Currency]]" shouldNot typeCheck
    "implicitly[ValueClassLike[Locale, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, Locale]]" shouldNot typeCheck
    "implicitly[ValueClassLike[UUID, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, UUID]]" shouldNot typeCheck
  }

  test("Currency standard format") {
    if (!isScalaJS && !isNative) {
      val codec = implicitly[JsonValueCodec[Currency]]
      val value = "PLN"
      val obj   = Currency.getInstance(value)

      writeToString[Currency](obj)(codec) shouldBe "\"" + value + "\""
      readFromString[Currency]("\"" + value + "\"")(codec) shouldBe obj
    }
  }

  test("Currency wrong format exception") {
    val codec = implicitly[JsonValueCodec[Currency]]
    val value = "not a Currency"

    an[Exception] should be thrownBy readFromString[Currency]("\"" + value + "\"")(codec)
  }

  test("Locale standard format") {
    val codec = implicitly[JsonValueCodec[Locale]]
    val value = "pl-PL"
    val obj   = Locale.forLanguageTag(value)

    writeToString[Locale](obj)(codec) shouldBe "\"" + value + "\""
    readFromString[Locale]("\"" + value + "\"")(codec) shouldBe obj
  }

  test("UUID standard format") {
    val codec = implicitly[JsonValueCodec[UUID]]
    val value = "123e4567-e89b-12d3-a456-426614174000"
    val obj   = UUID.fromString(value)

    writeToString[UUID](obj)(codec) shouldBe "\"" + value + "\""
    readFromString[UUID]("\"" + value + "\"")(codec) shouldBe obj
  }

  test("UUID wrong format exception") {
    val codec = implicitly[JsonValueCodec[UUID]]
    val value = "not an UUID"

    an[Exception] should be thrownBy readFromString[UUID]("\"" + value + "\"")(codec)
  }
}
