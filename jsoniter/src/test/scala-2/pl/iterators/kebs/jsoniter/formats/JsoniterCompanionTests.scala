package pl.iterators.kebs.jsoniter.formats

import com.github.plokhotnyuk.jsoniter_scala.core._

object JsoniterCompanionTestDomain {
  import JsoniterAaCompanion.Named
  import pl.iterators.kebs.jsoniter.snakified.deriveCodec

  def inMethod = {
    val codec: JsonValueCodec[Named] = deriveCodec[Named]
    writeToString(Named(1, 2))(codec)
  }
  val inValue = {
    implicit val codec: JsonValueCodec[Named] = deriveCodec[Named]
    readFromString[Named]("""{"some_field":1,"other_field":2}""")
  }
}

class JsoniterCompanionTests extends JsoniterTestBase {
  test("codecs with an inferred type in companions of other files") {
    JsoniterAaCompanion.written shouldBe """{"outer":{"OUTER":[{"x":1,"y":2},3]},"n":4}"""
    JsoniterZzCompanion.written shouldBe """{"outer":{"OUTER":[{"x":1,"y":2},3]},"n":4}"""
    JsoniterAaCompanion.read.n shouldBe 4
    JsoniterZzCompanion.read.n shouldBe 4
  }

  test("explicitly derived codecs next to inferred types") {
    JsoniterAaCompanion.writtenNamed shouldBe """{"named":{"some_field":1,"other_field":2},"n":3}"""
    JsoniterZzCompanion.writtenNamed shouldBe """{"named":{"some_field":1,"other_field":2},"n":3}"""
    JsoniterCompanionTestDomain.inMethod shouldBe """{"some_field":1,"other_field":2}"""
    JsoniterCompanionTestDomain.inValue shouldBe JsoniterAaCompanion.Named(1, 2)
  }
}
