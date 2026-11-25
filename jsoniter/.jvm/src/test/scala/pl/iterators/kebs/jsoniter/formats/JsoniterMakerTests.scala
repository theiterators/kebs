package pl.iterators.kebs.jsoniter.formats

import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros._
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.jsoniter.KebsJsoniter
import pl.iterators.kebs.testkit.model.Id
import pl.iterators.kebs.testkit.nested.Item

object JsoniterMakerTestDomain {
  object Protocol extends KebsJsoniter
  object Flat     extends KebsJsoniter with CaseClass1ToValueClass

  final case class Made(someField: Int, items: List[Item])
  object Made {
    implicit val codec: JsonValueCodec[Made] = JsonCodecMaker.make(CodecMakerConfig.withFieldNameMapper(JsonCodecMaker.enforce_snake_case))
  }
  final case class HasMade(made: Made, all: List[Made], byName: Map[String, Made])

  final case class Holder(item: Item, ids: List[Id], n: Int)
  final case class Wrapped(value: Int) extends AnyVal
  final case class HasWrapped(wrapped: Wrapped, n: Int)
  final case class Plain(a: List[Int], b: Map[String, String], c: Option[Vector[Long]])
}

// next to jsoniter-scala's own macro
class JsoniterMakerTests extends JsoniterTestBase {
  import JsoniterMakerTestDomain._

  test("a codec made by jsoniter's macro is used") {
    import Protocol._
    roundTrip(Made(1, List(Item("i", 2))), """{"some_field":1,"items":[{"name":"i","n":2}]}""")
    roundTrip(
      HasMade(Made(1, Nil), List(Made(2, Nil)), Map("k" -> Made(3, Nil))),
      """{"made":{"some_field":1},"all":[{"some_field":2}],"byName":{"k":{"some_field":3}}}"""
    )
  }

  test("a local codec made by jsoniter's macro is used") {
    import Protocol._
    implicit val item: JsonValueCodec[Item] = JsonCodecMaker.make(CodecMakerConfig.withFieldNameMapper(JsonCodecMaker.EnforcePascalCase))
    roundTrip(Holder(Item("i", 1), Nil, 2), """{"item":{"Name":"i","N":1},"ids":[],"n":2}""")
    roundTrip(List(Item("i", 1)), """[{"Name":"i","N":1}]""")
  }

  test("jsoniter's macro uses kebs' codecs that are values") {
    import Flat._
    implicit val item: JsonValueCodec[Item]       = deriveCodec[Item]
    implicit val id: JsonValueCodec[Id]           = deriveCodec[Id]
    implicit val wrapped: JsonValueCodec[Wrapped] = deriveCodec[Wrapped]
    val holder: JsonValueCodec[Holder]            = JsonCodecMaker.make
    val hasWrapped: JsonValueCodec[HasWrapped]    = JsonCodecMaker.make
    roundTrip(Holder(Item("i", 1), List(Id("x")), 2), """{"item":{"name":"i","n":1},"ids":["x"],"n":2}""")(holder)
    roundTrip(HasWrapped(Wrapped(1), 2), """{"wrapped":1,"n":2}""")(hasWrapped)
    rejected[Holder]("""{"item":{"name":null,"n":1},"ids":["x"],"n":2}""", "")(holder)
  }

  test("jsoniter's macro where a kebs protocol is imported") {
    import Protocol._
    val codec: JsonValueCodec[Plain] = JsonCodecMaker.make
    roundTrip(Plain(List(1), Map("a" -> "b"), Some(Vector(2L))), """{"a":[1],"b":{"a":"b"},"c":[2]}""")(codec)
  }
}
