package pl.iterators.kebs.circe.formats

import io.circe.parser.decode
import io.circe.syntax._
import io.circe.{Codec, Decoder, Encoder}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.circe.{KebsCirce, KebsCirceSemiauto}
import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.naming.Naming
import pl.iterators.kebs.testkit.semiauto._

class CirceSemiautoTests extends AnyFunSuite with Matchers {
  object Protocol extends KebsCirceSemiauto {
    override def kebsJsonConfig: JsonConfig = JsonConfig.default.withFieldNaming(Naming.snakeCase)
  }
  object Auto extends KebsCirce

  test("nothing is derived implicitly") {
    import Protocol._
    "implicitly[Encoder[Outer]]" shouldNot typeCheck
    "implicitly[Decoder[Outer]]" shouldNot typeCheck
  }

  test("derive* derives the type and, inside it, the types it needs, with the protocol's config") {
    import Protocol._
    implicit val encoder: Encoder.AsObject[Outer] = deriveEncoder[Outer]
    implicit val decoder: Decoder[Outer]          = deriveDecoder[Outer]
    outer.asJson.noSpaces shouldBe outerJson
    decode[Outer](outerJson) shouldBe Right(outer)
  }

  test("deriveCodec, also of a sealed hierarchy") {
    import Protocol._
    implicit val codec: Codec.AsObject[Shape] = deriveCodec[Shape]
    (Circle(3): Shape).asJson.noSpaces shouldBe circleJson
    decode[Shape](circleJson) shouldBe Right(Circle(3))
  }

  test("the full protocol has the same methods") {
    import Auto._
    val codec: Codec.AsObject[Inner] = deriveCodec[Inner]
    codec(Inner(1)).noSpaces shouldBe """{"fooBar":1}"""
  }
}
