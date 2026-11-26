package pl.iterators.kebs.sprayjson.formats

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.sprayjson.KebsSprayJson
import pl.iterators.kebs.testkit.nested._
import spray.json._

// ambiguous with kebs' entry point on Scala 3, where a macro cannot see which type class was requested
class SprayGenericFormatRequestTests extends AnyFunSuite with Matchers {
  object Protocol extends DefaultJsonProtocol with KebsSprayJson
  import Protocol._, SprayNestedInstancesTestDomain.PageFormats._

  test("a user's generic format in lexical scope, requested as JsonFormat") {
    implicitly[JsonFormat[Page[Item]]].write(Page(List(item))).compactPrint shouldBe """{"DATA":[{"n":1,"name":"i"}]}"""
    implicitly[JsonFormat[Page[RecPage]]].write(Page(List(recPage))).compactPrint shouldBe
    """{"DATA":[{"kids":{"DATA":[{"kids":{"DATA":[]},"v":2}]},"v":1}]}"""
  }
}
