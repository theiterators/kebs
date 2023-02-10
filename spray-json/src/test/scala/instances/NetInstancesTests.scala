package instances

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
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

  test("No CaseClass1Rep implicits derived") {
    import pl.iterators.kebs.core.CaseClass1Rep

    "implicitly[CaseClass1Rep[URI, String]]" shouldNot typeCheck
    "implicitly[CaseClass1Rep[String, URI]]" shouldNot typeCheck
  }
}
