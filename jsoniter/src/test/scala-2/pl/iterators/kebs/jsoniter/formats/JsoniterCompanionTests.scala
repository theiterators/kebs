package pl.iterators.kebs.jsoniter.formats

class JsoniterCompanionTests extends JsoniterTestBase {
  test("codecs with an inferred type in companions of other files") {
    JsoniterAaCompanion.written shouldBe """{"outer":{"OUTER":[{"x":1,"y":2},3]},"n":4}"""
    JsoniterZzCompanion.written shouldBe """{"outer":{"OUTER":[{"x":1,"y":2},3]},"n":4}"""
    JsoniterAaCompanion.read.n shouldBe 4
    JsoniterZzCompanion.read.n shouldBe 4
  }
}
