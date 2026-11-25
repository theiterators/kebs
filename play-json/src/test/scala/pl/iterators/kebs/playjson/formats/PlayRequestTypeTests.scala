package pl.iterators.kebs.playjson.formats

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.playjson.KebsPlayJson
import pl.iterators.kebs.testkit.nested._
import play.api.libs.json._

// the same type must be written the same way whichever type class it is requested as
class PlayRequestTypeTests extends AnyFunSuite with Matchers {
  object Protocol extends KebsPlayJson
  import Protocol._
  import PlayNestedInstancesTestDomain.PageInstances._

  private val adtJson = """{"Branch":{"l":{"DATA":[{"Leaf":{"n":1,"i":{"name":"i","n":1}}},{"Empty":{}}]},"r":[{"Empty":{}}]}}"""
  private val recJson = """{"v":1,"kids":{"DATA":[{"v":2,"kids":{"DATA":[]}}]}}"""
  private val mutJson = """{"name":"a","b":{"as":{"DATA":[{"name":"b"}]},"n":2}}"""

  private def written(json: JsValue) = Json.stringify(json)

  test("a sealed hierarchy") {
    written(implicitly[Writes[Adt]].writes(adt)) shouldBe adtJson
    written(implicitly[OWrites[Adt]].writes(adt)) shouldBe adtJson
    written(implicitly[OFormat[Adt]].writes(adt)) shouldBe adtJson
    written(deriveWrites[Adt].writes(adt)) shouldBe adtJson
    written(Json.toJson(adt)) shouldBe adtJson
    implicitly[Reads[Adt]].reads(Json.parse(adtJson)) shouldBe JsSuccess(adt)
    implicitly[OFormat[Adt]].reads(Json.parse(adtJson)) shouldBe JsSuccess(adt)
    Json.parse(adtJson).as[Adt] shouldBe adt
  }

  test("a recursive product") {
    written(implicitly[Writes[RecPage]].writes(recPage)) shouldBe recJson
    written(implicitly[OWrites[RecPage]].writes(recPage)) shouldBe recJson
    written(implicitly[OFormat[RecPage]].writes(recPage)) shouldBe recJson
    written(deriveWrites[RecPage].writes(recPage)) shouldBe recJson
    written(deriveFormat[RecPage].writes(recPage)) shouldBe recJson
    written(Json.toJson(recPage)) shouldBe recJson
    implicitly[Reads[RecPage]].reads(Json.parse(recJson)) shouldBe JsSuccess(recPage)
    deriveReads[RecPage].reads(Json.parse(recJson)) shouldBe JsSuccess(recPage)
  }

  test("inside containers") {
    written(Json.toJson(List(recPage))) shouldBe s"[$recJson]"
    written(Json.toJson(Map("k" -> recPage))) shouldBe s"""{"k":$recJson}"""
    written(Json.toJson((1, recPage))) shouldBe s"[1,$recJson]"
    written(Json.toJson(List(adt))) shouldBe s"[$adtJson]"
    Json.parse(s"[$recJson]").as[List[RecPage]] shouldBe List(recPage)
  }

  test("mutually recursive products") {
    written(implicitly[Writes[MutA]].writes(mutA)) shouldBe mutJson
    written(implicitly[Writes[MutB]].writes(mutA.b.get)) shouldBe """{"as":{"DATA":[{"name":"b"}]},"n":2}"""
    implicitly[Reads[MutA]].reads(Json.parse(mutJson)) shouldBe JsSuccess(mutA)
  }
}
