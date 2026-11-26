package pl.iterators.kebs.playjson.formats

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.naming.Naming
import pl.iterators.kebs.playjson.{KebsPlayJson, KebsPlayJsonSemiauto}
import pl.iterators.kebs.testkit.semiauto._
import play.api.libs.json._

class PlaySemiautoTests extends AnyFunSuite with Matchers {
  object Protocol extends KebsPlayJsonSemiauto {
    override def kebsJsonConfig: JsonConfig = super.kebsJsonConfig.withFieldNaming(Naming.snakeCase)
  }
  object Auto extends KebsPlayJson

  test("nothing is derived implicitly") {
    import Protocol._
    "implicitly[Reads[Outer]]" shouldNot typeCheck
    "implicitly[OWrites[Outer]]" shouldNot typeCheck
  }

  test("deriveFormat derives the type and, inside it, the types it needs, with the protocol's config") {
    import Protocol._
    implicit val format: OFormat[Outer] = deriveFormat[Outer]
    Json.stringify(Json.toJson(outer)) shouldBe outerJson
    Json.parse(outerJson).as[Outer] shouldBe outer
  }

  test("deriveReads and deriveWrites of a sealed hierarchy") {
    import Protocol._
    implicit val reads: Reads[Shape]    = deriveReads[Shape]
    implicit val writes: OWrites[Shape] = deriveWrites[Shape]
    Json.stringify(Json.toJson(Circle(3): Shape)) shouldBe circleJson
    Json.parse(circleJson).as[Shape] shouldBe Circle(3)
  }

  test("the full protocol has the same methods") {
    import Auto._
    Json.stringify(deriveWrites[Inner].writes(Inner(1))) shouldBe """{"fooBar":1}"""
  }
}
