package pl.iterators.kebs.playjson.instances

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.instances.InstanceConverter.DecodeErrorException
import pl.iterators.kebs.instances.net.URIString
import play.api.libs.json.{Format, JsError, JsString, JsSuccess}

import java.net.URI

class NetInstancesTests extends AnyFunSuite with Matchers with URIString {
  import pl.iterators.kebs.playjson._

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

    jf.reads(JsString(value)) shouldBe a[JsError]
  }

  test("No ValueClassLike implicits derived") {

    "implicitly[ValueClassLike[URI, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, URI]]" shouldNot typeCheck
  }
}
