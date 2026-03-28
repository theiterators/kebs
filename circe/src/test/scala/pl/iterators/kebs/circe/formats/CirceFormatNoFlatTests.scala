package pl.iterators.kebs.circe.formats

import io.circe.{Decoder, Encoder, Json}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.circe.KebsCirce
import pl.iterators.kebs.circe.model._

class CirceFormatNoFlatTests extends AnyFunSuite with Matchers {
  object KebsProtocol extends KebsCirce
  import KebsProtocol._

  test("No-flat format for 1-element case class") {
    val decoder = implicitly[Decoder[C]]
    val encoder = implicitly[Encoder[C]]
    decoder.apply(Json.fromFields(Seq("anInteger" -> Json.fromInt(10))).hcursor) shouldBe Right(C(10))
    encoder.apply(C(10)) shouldBe Json.fromFields(Seq("anInteger" -> Json.fromInt(10)))
  }

  test("No-flat format - parametrized") {
    val decoder = implicitly[Decoder[Parametrized1[Double]]]
    val encoder = implicitly[Encoder[Parametrized1[Double]]]
    decoder.apply(Json.fromFields(Seq("field" -> Json.fromDouble(15.0).get)).hcursor) shouldBe Right(Parametrized1(15.0))
    encoder.apply(Parametrized1(15.0)) shouldBe Json.fromFields(Seq("field" -> Json.fromDouble(15.0).get))
  }

  test("No-flat format - DTO style with 1-element case class field") {
    val decoder      = implicitly[Decoder[DTO1]]
    val encoder      = implicitly[Encoder[DTO1]]
    val expectedJson = Json.fromFields(
      Seq(
        "c" -> Json.fromFields(Seq("anInteger" -> Json.fromInt(10))),
        "i" -> Json.fromInt(5)
      )
    )
    decoder.apply(expectedJson.hcursor) shouldBe Right(DTO1(C(10), 5))
    encoder.apply(DTO1(C(10), 5)) shouldBe expectedJson
  }
}
