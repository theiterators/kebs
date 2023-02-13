package instances

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.converters.InstanceConverter.DecodeErrorException
import pl.iterators.kebs.instances.net.URIString
import play.api.libs.json.{Format, JsString, JsSuccess}

import java.net.URI

class NetInstancesTests extends AnyFunSuite with Matchers with URIString {
  import pl.iterators.kebs.json._

  test("URI standard format") {
    val jf    = implicitly[Format[URI]]
    val value = "iteratorshq.com"
    val obj   = new URI(value)

    jf.writes(obj) shouldBe JsString(value)
    jf.reads(JsString(value)) shouldBe JsSuccess(obj)
  }

  test("URI wrong format exception") {
    val jf    = implicitly[Format[URI]]
    val value = "not a URI"

    assertThrows[DecodeErrorException](jf.reads(JsString(value)))
  }

  test("No CaseClass1Rep implicits derived") {

    "implicitly[CaseClass1Rep[URI, String]]" shouldNot typeCheck
    "implicitly[CaseClass1Rep[String, URI]]" shouldNot typeCheck
  }
}
