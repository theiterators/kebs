package pl.iterators.kebs.sprayjson.formats

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.sprayjson.KebsSprayJson
import pl.iterators.kebs.testkit.nested._
import spray.json._

// the same type must be written the same way whichever type class it is requested as
class SprayRequestTypeTests extends AnyFunSuite with Matchers {
  object Protocol extends DefaultJsonProtocol with KebsSprayJson
  import Protocol._
  import SprayNestedInstancesTestDomain.PageFormats._

  private val adtJson = """{"Branch":{"l":{"DATA":[{"Leaf":{"i":{"n":1,"name":"i"},"n":1}},{"Empty":{}}]},"r":[{"Empty":{}}]}}"""
  private val recJson = """{"kids":{"DATA":[{"kids":{"DATA":[]},"v":2}]},"v":1}"""
  private val mutJson = """{"b":{"as":{"DATA":[{"name":"b"}]},"n":2},"name":"a"}"""

  test("a sealed hierarchy") {
    implicitly[RootJsonFormat[Adt]].write(adt).compactPrint shouldBe adtJson
    implicitly[JsonFormat[Adt]].write(adt).compactPrint shouldBe adtJson
    implicitly[JsonWriter[Adt]].write(adt).compactPrint shouldBe adtJson
    implicitly[RootJsonWriter[Adt]].write(adt).compactPrint shouldBe adtJson
    adt.toJson.compactPrint shouldBe adtJson
    implicitly[JsonReader[Adt]].read(adtJson.parseJson) shouldBe adt
    adtJson.parseJson.convertTo[Adt] shouldBe adt
  }

  test("a recursive product") {
    implicitly[RootJsonFormat[RecPage]].write(recPage).compactPrint shouldBe recJson
    implicitly[JsonFormat[RecPage]].write(recPage).compactPrint shouldBe recJson
    implicitly[JsonWriter[RecPage]].write(recPage).compactPrint shouldBe recJson
    implicitly[RootJsonWriter[RecPage]].write(recPage).compactPrint shouldBe recJson
    recPage.toJson.compactPrint shouldBe recJson
    implicitly[RootJsonReader[RecPage]].read(recJson.parseJson) shouldBe recPage
    recJson.parseJson.convertTo[RecPage] shouldBe recPage
  }

  test("inside containers") {
    implicitly[JsonFormat[List[RecPage]]].write(List(recPage)).compactPrint shouldBe s"[$recJson]"
    implicitly[JsonFormat[Map[String, RecPage]]].write(Map("k" -> recPage)).compactPrint shouldBe s"""{"k":$recJson}"""
    implicitly[JsonFormat[(Int, RecPage)]].write((1, recPage)).compactPrint shouldBe s"[1,$recJson]"
    implicitly[JsonFormat[Option[RecPage]]].write(Some(recPage)).compactPrint shouldBe recJson
    implicitly[JsonFormat[List[Adt]]].write(List(adt)).compactPrint shouldBe s"[$adtJson]"
  }

  test("mutually recursive products") {
    implicitly[JsonFormat[MutA]].write(mutA).compactPrint shouldBe mutJson
    implicitly[JsonFormat[List[MutA]]].write(List(mutA)).compactPrint shouldBe s"[$mutJson]"
    implicitly[JsonFormat[MutB]].write(mutA.b.get).compactPrint shouldBe """{"as":{"DATA":[{"name":"b"}]},"n":2}"""
  }
}
