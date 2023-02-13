package pl.iterators.kebs.instances.net

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.converters.InstanceConverter
import InstanceConverter.DecodeErrorException

import java.net.URI

class NetInstancesTests extends AnyFunSuite with Matchers with URIString {

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
