package pl.iterators.kebs.instances.util

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pl.iterators.kebs.instances.InstanceConverter

import java.util.Locale

class UtilInstancesTests extends AnyFunSuite with LocaleString {
  test("Locale to String") {
    val ico   = implicitly[InstanceConverter[Locale, String]]
    val value = "pl-PL"
    val obj   = Locale.forLanguageTag(value)

    ico.decode(value) shouldEqual obj
    ico.encode(obj) shouldEqual value
  }
}
