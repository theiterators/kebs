package pl.iterators.kebs.jsoniter.instances

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.jsoniter.KebsJsoniter
import pl.iterators.kebs.instances.net.URIString

import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.core.JsonReaderException
import com.github.plokhotnyuk.jsoniter_scala.macros._
import java.net.URI

class NetInstancesTests extends AnyFunSuite with Matchers with KebsJsoniter with URIString {

  test("URI standard format") {
    val codec = implicitly[JsonValueCodec[URI]]
    val value = "iteratorshq.com"
    val obj   = new URI(value)

    writeToString(obj)(using codec) shouldBe s"\"${value}\""
    readFromString[URI](s"\"${value}\"") shouldBe obj
  }

  test("URI wrong format exception") {
    val codec = implicitly[JsonValueCodec[URI]]
    val value = "not a URI"

    a[JsonReaderException] should be thrownBy readFromString(value)(using codec)
  }

  test("No ValueClassLike implicits derived") {

    "implicitly[ValueClassLike[URI, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, URI]]" shouldNot typeCheck
  }

}
