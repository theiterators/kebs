package pl.iterators.kebs.instances

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.instances.NetInstances.URIString
import pl.iterators.kebs.json.KebsSpray
import spray.json._

import java.net.URI

class NetInstancesTests extends AnyFunSuite with Matchers with DefaultJsonProtocol with KebsSpray with NetInstances with URIString {

  test("URI standard format") {
    val jf    = implicitly[JsonFormat[URI]]
    val value = "iteratorshq.com"
    val obj   = new URI(value)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("URI wrong format exception") {
    import NetInstances.URIFormat

    val jf    = implicitly[JsonFormat[URI]]
    val value = "not a URI"

    val thrown = intercept[IllegalArgumentException] {
      jf.read(JsString(value))
    }
    assert(thrown.getMessage === errorMessage[URI, String](classOf[URI], value, URIFormat))
  }
}
