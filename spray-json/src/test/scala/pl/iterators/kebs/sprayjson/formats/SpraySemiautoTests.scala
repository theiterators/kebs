package pl.iterators.kebs.sprayjson.formats

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.naming.Naming
import pl.iterators.kebs.sprayjson.{KebsSprayJson, KebsSprayJsonSemiauto}
import pl.iterators.kebs.testkit.semiauto._
import spray.json._

import scala.annotation.nowarn

class SpraySemiautoTests extends AnyFunSuite with Matchers {
  object Protocol extends DefaultJsonProtocol with KebsSprayJsonSemiauto {
    override def kebsJsonConfig: JsonConfig = super.kebsJsonConfig.withFieldNaming(Naming.snakeCase)
  }
  object Auto extends DefaultJsonProtocol with KebsSprayJson

  test("nothing is derived implicitly") {
    import Protocol._
    "implicitly[RootJsonFormat[Outer]]" shouldNot typeCheck
  }

  test("deriveFormat derives the type and, inside it, the types it needs, with the protocol's config") {
    import Protocol._
    implicit val format: RootJsonFormat[Outer] = deriveFormat[Outer]
    outer.toJson shouldBe outerJson.parseJson // spray-json sorts the members
    outerJson.parseJson.convertTo[Outer] shouldBe outer
  }

  test("deriveFormat of a sealed hierarchy") {
    import Protocol._
    implicit val format: RootJsonFormat[Shape] = deriveFormat[Shape]
    (Circle(3): Shape).toJson.compactPrint shouldBe circleJson
    circleJson.parseJson.convertTo[Shape] shouldBe Circle(3)
  }

  test("the full protocol has deriveFormat") {
    import Auto._
    deriveFormat[Inner].write(Inner(1)).compactPrint shouldBe """{"fooBar":1}"""
  }

  test("jsonFormatN and jsonFormatSealed still work") {
    import Auto._
    (jsonFormatN[Inner]: @nowarn("cat=deprecation")).write(Inner(1)).compactPrint shouldBe """{"fooBar":1}"""
    (jsonFormatSealed[Shape]: @nowarn("cat=deprecation")).write(Empty).compactPrint shouldBe """{"Empty":{}}"""
  }
}
