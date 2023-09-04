package instances

import io.circe.{Decoder, Encoder, Json}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.circe.KebsCirce
import pl.iterators.kebs.instances.net.URIString

import java.net.URI

class NetInstancesTests extends AnyFunSuite with Matchers with KebsCirce with URIString {

  test("URI standard format") {
    val decoder    = implicitly[Decoder[URI]]
    val encoder    = implicitly[Encoder[URI]]
    val value = "iteratorshq.com"
    val obj   = new URI(value)

    encoder(obj) shouldBe Json.fromString(value)
    decoder(Json.fromString(value).hcursor) shouldBe Right(obj)
  }

  test("URI wrong format exception") {
    val decoder    = implicitly[Decoder[URI]]
    val value = "not a URI"

    decoder(Json.fromString(value).hcursor) shouldBe a [Left[_, _]]
  }

  test("No ValueClassLike implicits derived") {

    "implicitly[ValueClassLike[URI, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, URI]]" shouldNot typeCheck
  }
}
