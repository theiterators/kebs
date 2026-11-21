package pl.iterators.kebs.circe.golden

import io.circe.{Decoder, Encoder, Json}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.circe.KebsCirce
import pl.iterators.kebs.testkit.model._

/** Scala 2 only: kebs 2 on Scala 3 does not derive sealed traits. The kebs 3 circe plan moves this to shared tests. */
class CirceAdtGoldenTests extends AnyFunSuite with Matchers {
  private def json(s: String): Json = io.circe.parser.parse(s).fold(throw _, identity)

  object Default extends KebsCirce
  import Default._

  test("sealed trait - case class child (wrapper object)") {
    val value: Shape = Circle(1.0, "c")
    implicitly[Encoder[Shape]].apply(value) shouldBe json("""{"Circle":{"r":1.0,"label":"c"}}""")
    implicitly[Decoder[Shape]].decodeJson(json("""{"Circle":{"r":1.0,"label":"c"}}""")) shouldBe Right(value)
  }

  test("sealed trait - case object child (wrapper object)") {
    val value: Shape = Empty
    implicitly[Encoder[Shape]].apply(value) shouldBe json("""{"Empty":{}}""")
    implicitly[Decoder[Shape]].decodeJson(json("""{"Empty":{}}""")) shouldBe Right(value)
  }
}
