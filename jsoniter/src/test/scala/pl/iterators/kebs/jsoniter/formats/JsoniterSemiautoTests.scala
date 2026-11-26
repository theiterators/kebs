package pl.iterators.kebs.jsoniter.formats

import com.github.plokhotnyuk.jsoniter_scala.core._
import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.naming.Naming
import pl.iterators.kebs.jsoniter.{KebsJsoniter, KebsJsoniterSemiauto}
import pl.iterators.kebs.testkit.semiauto._

class JsoniterSemiautoTests extends JsoniterTestBase {
  object Protocol extends KebsJsoniterSemiauto {
    override def kebsJsonConfig: JsonConfig = super.kebsJsonConfig.withFieldNaming(Naming.snakeCase)
  }
  object Auto extends KebsJsoniter

  test("nothing is derived implicitly") {
    import Protocol._
    "implicitly[JsonValueCodec[Outer]]" shouldNot typeCheck
  }

  test("deriveCodec derives the type and, inside it, the types it needs, with the protocol's config") {
    import Protocol._
    roundTrip(outer, outerJson)(deriveCodec[Outer])
    roundTrip(Circle(3): Shape, circleJson)(deriveCodec[Shape])
  }

  test("the full protocol has the same method") {
    import Auto._
    roundTrip(Inner(1), """{"fooBar":1}""")(deriveCodec[Inner])
  }
}
