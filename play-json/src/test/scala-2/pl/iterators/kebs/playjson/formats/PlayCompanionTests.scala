package pl.iterators.kebs.playjson.formats

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class PlayCompanionTests extends AnyFunSuite with Matchers {
  test("formats with an inferred type in companions of other files") {
    PlayAaCompanion.written shouldBe """{"outer":{"inner":{"x":1,"y":2},"n":3},"n":4}"""
    PlayZzCompanion.written shouldBe """{"outer":{"inner":{"x":1,"y":2},"n":3},"n":4}"""
  }
}
