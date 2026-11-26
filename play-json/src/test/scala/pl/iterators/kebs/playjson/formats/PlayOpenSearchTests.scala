package pl.iterators.kebs.playjson.formats

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.playjson.KebsPlayJson
import pl.iterators.kebs.testkit.nested._
import play.api.libs.json._

object PlayOpenSearchTests {
  final case class HasRec(rec: RecPage, adt: Adt)
}

// derivation that the compiler reaches while another macro's search is open
class PlayOpenSearchTests extends AnyFunSuite with Matchers {
  import PlayOpenSearchTests._
  object Protocol extends KebsPlayJson
  import Protocol._, PlayNestedInstancesTestDomain.PageInstances._

  test("inside play's own macro") {
    val format = Json.format[HasRec]
    val json   = """{"rec":{"v":1,"kids":{"DATA":[{"v":2,"kids":{"DATA":[]}}]}},""" +
      """"adt":{"Branch":{"l":{"DATA":[{"Leaf":{"n":1,"i":{"name":"i","n":1}}},{"Empty":{}}]},"r":[{"Empty":{}}]}}}"""
    Json.stringify(format.writes(HasRec(recPage, adt))) shouldBe json
    format.reads(Json.parse(json)) shouldBe JsSuccess(HasRec(recPage, adt))
  }
}
