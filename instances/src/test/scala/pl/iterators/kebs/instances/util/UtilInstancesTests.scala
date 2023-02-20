package pl.iterators.kebs.instances.util

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.instances.InstanceConverter
import pl.iterators.kebs.instances.InstanceConverter.DecodeErrorException
import pl.iterators.kebs.instances.UtilInstances

import java.util.{Currency, Locale, UUID}

class UtilInstancesTests extends AnyFunSuite with Matchers with UtilInstances {

  test("Currency to String") {
    val ico   = implicitly[InstanceConverter[Currency, String]]
    val value = "PLN"
    val obj   = Currency.getInstance(value)

    ico.encode(obj) shouldBe value
    ico.decode(value) shouldBe obj
  }

  test("Currency wrong format exception") {
    val ico   = implicitly[InstanceConverter[Currency, String]]
    val value = "not a Currency"

    assertThrows[DecodeErrorException](ico.decode(value))
  }

  test("Locale to String") {
    val ico   = implicitly[InstanceConverter[Locale, String]]
    val value = "pl-PL"
    val obj   = Locale.forLanguageTag(value)

    ico.encode(obj) shouldBe value
    ico.decode(value) shouldBe obj
  }

  test("UUID to String") {
    val ico   = implicitly[InstanceConverter[UUID, String]]
    val value = "123e4567-e89b-12d3-a456-426614174000"
    val obj   = UUID.fromString(value)

    ico.encode(obj) shouldBe value
    ico.decode(value) shouldBe obj
  }

  test("UUID wrong format exception") {
    val ico   = implicitly[InstanceConverter[UUID, String]]
    val value = "not an UUID"

    assertThrows[DecodeErrorException](ico.decode(value))
  }
}
