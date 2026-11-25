package pl.iterators.kebs.circe.golden

import io.circe.{Decoder, Encoder, Json}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.circe.{KebsCirce, KebsCirceSnakified}

object CirceChangedFormatTestDomain {
  final case class Point(x: Int, y: Int)
  object Point {
    implicit val encoder: Encoder[Point] = Encoder.encodeString.contramap(p => s"${p.x},${p.y}")
    implicit val decoder: Decoder[Point] = Decoder.decodeString.map { s =>
      val Array(x, y) = s.split(',')
      Point(x.toInt, y.toInt)
    }
  }
  final case class PresetFields(pairValue: (Int, String), atPoint: Point, someValue: Some[Int])
}

// Deliberate changes from kebs 2.3.0, listed in the migration guide. kebs 2's presets (and on Scala 3 every protocol)
// derived any product in scope, beating companion and circe instances: it wrote {"_1":3,"_2":"a"}, {"x":1,"y":2} and
// {"value":5} here. In kebs 3 existing instances win.
class CirceChangedFormatTests extends AnyFunSuite with Matchers {
  import CirceChangedFormatTestDomain._

  private def json(s: String): Json = io.circe.parser.parse(s).fold(throw _, identity)

  object Default   extends KebsCirce
  object Snakified extends KebsCirceSnakified

  private val fields = PresetFields((3, "a"), Point(1, 2), Some(5))

  test("default protocol: tuple, companion and Some instances win") {
    import Default._
    val expected = json("""{"pairValue":[3,"a"],"atPoint":"1,2","someValue":5}""")
    implicitly[Encoder[PresetFields]].apply(fields) shouldBe expected
    implicitly[Decoder[PresetFields]].decodeJson(expected) shouldBe Right(fields)
  }

  test("snakified preset: tuple, companion and Some instances win") {
    import Snakified._
    val expected = json("""{"pair_value":[3,"a"],"at_point":"1,2","some_value":5}""")
    implicitly[Encoder[PresetFields]].apply(fields) shouldBe expected
    implicitly[Decoder[PresetFields]].decodeJson(expected) shouldBe Right(fields)
  }
}
