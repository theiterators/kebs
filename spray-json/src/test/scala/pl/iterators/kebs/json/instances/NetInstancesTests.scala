package pl.iterators.kebs.json.instances

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.instances.net.URIString
import pl.iterators.kebs.core.instances.InstanceConverter.DecodeErrorException
import pl.iterators.kebs.json.KebsSpray
import spray.json._

import java.net.URI

class NetInstancesTests extends AnyFunSuite with Matchers with DefaultJsonProtocol with KebsSpray with URIString {

  test("URI standard format") {
    val jf    = implicitly[JsonFormat[URI]]
    val value = "iteratorshq.com"
    val obj   = new URI(value)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("URI wrong format exception") {
    val jf    = implicitly[JsonFormat[URI]]
    val value = "not a URI"

    assertThrows[DecodeErrorException](jf.read(JsString(value)))
  }

  test("No ValueClassLike implicits derived") {
    import pl.iterators.kebs.core.macros.ValueClassLike

    "implicitly[ValueClassLike[URI, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, URI]]" shouldNot typeCheck
  }
}
