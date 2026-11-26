package pl.iterators.kebs.circe.formats

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class CirceCompanionTests extends AnyFunSuite with Matchers {
  test("codecs with an inferred type in companions of other files") {
    CirceAaCompanion.written shouldBe """{"outer":{"INNER":{"x":1,"y":2},"n":3},"n":4}"""
    CirceZzCompanion.written shouldBe """{"outer":{"INNER":{"x":1,"y":2},"n":3},"n":4}"""
    CirceAaCompanion.read shouldBe true
    CirceZzCompanion.read shouldBe true
  }
}
