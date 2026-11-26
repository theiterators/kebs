package pl.iterators.kebs.circe.formats

import io.circe.{Decoder, Encoder}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.circe.KebsCirce
import pl.iterators.kebs.testkit.nested._

// the same type must be written the same way whichever type class it is requested as
class CirceRequestTypeTests extends AnyFunSuite with Matchers {
  object Protocol extends KebsCirce
  import Protocol._
  import CirceNestedInstancesTestDomain.PageCodecs._

  private val adtJson = """{"Branch":{"l":{"DATA":[{"Leaf":{"n":1,"i":{"name":"i","n":1}}},{"Empty":{}}]},"r":[{"Empty":{}}]}}"""
  private val recJson = """{"v":1,"kids":{"DATA":[{"v":2,"kids":{"DATA":[]}}]}}"""
  private val mutJson = """{"name":"a","b":{"as":{"DATA":[{"name":"b","b":null}]},"n":2}}"""

  test("a sealed hierarchy") {
    Encoder[Adt].apply(adt).noSpaces shouldBe adtJson
    Encoder.AsObject[Adt].apply(adt).noSpaces shouldBe adtJson
    io.circe.parser.decode[Adt](adtJson) shouldBe Right(adt)
  }

  test("a case object") {
    Encoder[Empty.type].apply(Empty).noSpaces shouldBe "{}"
    Encoder.AsObject[Empty.type].apply(Empty).noSpaces shouldBe "{}"
    io.circe.parser.decode[Empty.type]("{}") shouldBe Right(Empty)
  }

  test("a recursive product") {
    Encoder[RecPage].apply(recPage).noSpaces shouldBe recJson
    Encoder.AsObject[RecPage].apply(recPage).noSpaces shouldBe recJson
    io.circe.parser.decode[RecPage](recJson) shouldBe Right(recPage)
  }

  test("inside containers") {
    Encoder[List[RecPage]].apply(List(recPage)).noSpaces shouldBe s"[$recJson]"
    Encoder[Map[String, RecPage]].apply(Map("k" -> recPage)).noSpaces shouldBe s"""{"k":$recJson}"""
    Encoder[Page[RecPage]].apply(Page(List(recPage))).noSpaces shouldBe s"""{"DATA":[$recJson]}"""
    Encoder[(Int, RecPage)].apply((1, recPage)).noSpaces shouldBe s"[1,$recJson]"
    Encoder[Either[Int, RecPage]].apply(Right(recPage)).noSpaces shouldBe s"""{"Right":{"value":$recJson}}"""
    Encoder[List[Adt]].apply(List(adt)).noSpaces shouldBe s"[$adtJson]"
    Decoder[List[RecPage]].decodeJson(Encoder[List[RecPage]].apply(List(recPage))) shouldBe Right(List(recPage))
  }

  test("mutually recursive products") {
    Encoder[MutA].apply(mutA).noSpaces shouldBe mutJson
    Encoder[MutB].apply(mutA.b.get).noSpaces shouldBe """{"as":{"DATA":[{"name":"b","b":null}]},"n":2}"""
    io.circe.parser.decode[MutA](mutJson) shouldBe Right(mutA)
  }
}
