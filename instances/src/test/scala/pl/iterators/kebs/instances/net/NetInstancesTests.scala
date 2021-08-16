package pl.iterators.kebs.instances.net

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pl.iterators.kebs.instances.InstanceConverter
import pl.iterators.kebs.instances.InstanceConverter.DecodeErrorException

import java.net.URI

class NetInstancesTests extends AnyFunSuite with URIString {

  test("URI to String") {
    val ico   = implicitly[InstanceConverter[URI, String]]
    val value = "www.test.pl"
    val obj   = new URI(value)

    ico.decode(value) shouldEqual obj
    ico.encode(obj) shouldEqual value
  }

  test("URI wrong format exception") {
    val ico   = implicitly[InstanceConverter[URI, String]]
    val value = "not a URI"

    assertThrows[DecodeErrorException](ico.decode(value))
  }
}
