package pl.iterators.kebs.sprayjson.formats

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class SprayCompanionTests extends AnyFunSuite with Matchers {
  test("formats with an inferred type in companions of other files") {
    SprayAaCompanion.written shouldBe """{"n":4,"outer":{"inner":{"x":1,"y":2},"n":3}}"""
    SprayZzCompanion.written shouldBe """{"n":4,"outer":{"inner":{"x":1,"y":2},"n":3}}"""
  }
}
