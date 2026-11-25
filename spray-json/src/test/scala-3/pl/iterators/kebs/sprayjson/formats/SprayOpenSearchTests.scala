package pl.iterators.kebs.sprayjson.formats

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.sprayjson.KebsSprayJson
import pl.iterators.kebs.testkit.nested._
import spray.json._

import scala.compiletime.summonInline

object SprayOpenSearchTests {
  final class Summoned[A](val format: JsonFormat[A])
  object Summoned {
    inline given [A]: Summoned[A] = new Summoned[A](summonInline[JsonFormat[A]])
  }
}

// derivation that the compiler reaches while another macro's search is open
class SprayOpenSearchTests extends AnyFunSuite with Matchers {
  import SprayOpenSearchTests._
  object Protocol extends DefaultJsonProtocol with KebsSprayJson
  import Protocol._, SprayNestedInstancesTestDomain.PageFormats._

  test("summoned by an inline given") {
    summon[Summoned[RecPage]].format.write(recPage).compactPrint shouldBe """{"kids":{"DATA":[{"kids":{"DATA":[]},"v":2}]},"v":1}"""
    summon[Summoned[Adt]].format.write(adt).compactPrint shouldBe
    """{"Branch":{"l":{"DATA":[{"Leaf":{"i":{"n":1,"name":"i"},"n":1}},{"Empty":{}}]},"r":[{"Empty":{}}]}}"""
    summon[Summoned[MutA]].format.write(mutA).compactPrint shouldBe """{"b":{"as":{"DATA":[{"name":"b"}]},"n":2},"name":"a"}"""
  }
}
