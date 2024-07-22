package pl.iterators.kebs.circe

import io.circe.{Decoder, Encoder, Json}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.circe.model._
import scala.Right

class CirceFormatSnakifiedVariantTests extends AnyFunSuite with Matchers {
  object KebsProtocol extends KebsCirce with KebsCirce.Snakified
  import KebsProtocol.{given, _}

  test("Flat format remains unchanged") {
    val decoder = implicitly[Decoder[C]]
    val encoder = implicitly[Encoder[C]]
    decoder.apply(Json.fromInt(10).hcursor) shouldBe Right(C(10))
    encoder.apply(C(10)) shouldBe Json.fromInt(10)
  }

  test("Format 0 remains unchanged") {
    val decoder = implicitly[Decoder[F.type]]
    val encoder = implicitly[Encoder[F.type]]
    decoder.apply(Json.fromFields(Seq.empty[(String, Json)]).hcursor) shouldBe Right(F)
    encoder.apply(F) shouldBe Json.fromFields(Seq.empty[(String, Json)])
  }

  test("format 2 snakified") {
    val decoder = implicitly[Decoder[D]]
    val encoder = implicitly[Encoder[D]]
    decoder
      .apply(Json.fromFields(Seq("int_field" -> Json.fromInt(10), "string_field" -> Json.fromString("abcd"))).hcursor) shouldBe Right(
      D(10, "abcd")
    )
    encoder.apply(D(10, "abcd")) shouldBe Json.fromFields(Seq("int_field" -> Json.fromInt(10), "string_field" -> Json.fromString("abcd")))
  }

  test("Format snakified - compound") {
    val decoder = implicitly[Decoder[Compound]]
    val encoder = implicitly[Encoder[Compound]]

    encoder.apply(Compound(C(5), D(10, "abcd"))) shouldBe Json.fromFields(
      Seq(
        "c_field" -> Json.fromInt(5),
        "d_field" -> Json.fromFields(Seq("int_field" -> Json.fromInt(10), "string_field" -> Json.fromString("abcd")))
      )
    )
    decoder
      .apply(
        Json
          .fromFields(
            Seq(
              "c_field" -> Json.fromInt(5),
              "d_field" -> Json.fromFields(Seq("int_field" -> Json.fromInt(10), "string_field" -> Json.fromString("abcd")))
            )
          )
          .hcursor
      ) shouldBe Right(Compound(C(5), D(10, "abcd")))
  }

  test("Format snakified - case class with > 22 fields") {
    import model._

    val decoder = implicitly[Decoder[ClassWith23Fields]]
    val encoder = implicitly[Encoder[ClassWith23Fields]]
    val obj = ClassWith23Fields(
      F1("f1 value"),
      2,
      3L,
      None,
      Some("f5 value"),
      "six",
      List("f7 value 1", "f7 value 2"),
      "f8 value",
      "f9 value",
      "f10 value",
      "f11 value",
      "f12 value",
      "f13 value",
      "f14 value",
      "f15 value",
      "f16 value",
      "f17 value",
      "f18 value",
      "f19 value",
      "f20 value",
      "f21 value",
      "f22 value",
      f23 = true
    )
    val json = Json.fromFields(
      Seq(
        "f1"               -> Json.fromString("f1 value"),
        "f2"               -> Json.fromInt(2),
        "f3"               -> Json.fromInt(3),
        "f4"               -> Json.Null,
        "f5"               -> Json.fromString("f5 value"),
        "field_number_six" -> Json.fromString("six"),
        "f7"               -> Json.arr(Json.fromString("f7 value 1"), Json.fromString("f7 value 2")),
        "f8"               -> Json.fromString("f8 value"),
        "f9"               -> Json.fromString("f9 value"),
        "f10"              -> Json.fromString("f10 value"),
        "f11"              -> Json.fromString("f11 value"),
        "f12"              -> Json.fromString("f12 value"),
        "f13"              -> Json.fromString("f13 value"),
        "f14"              -> Json.fromString("f14 value"),
        "f15"              -> Json.fromString("f15 value"),
        "f16"              -> Json.fromString("f16 value"),
        "f17"              -> Json.fromString("f17 value"),
        "f18"              -> Json.fromString("f18 value"),
        "f19"              -> Json.fromString("f19 value"),
        "f20"              -> Json.fromString("f20 value"),
        "f21"              -> Json.fromString("f21 value"),
        "f22"              -> Json.fromString("f22 value"),
        "f23"              -> Json.fromBoolean(true)
      )
    )

    encoder.apply(obj) shouldBe json
    decoder.apply(json.hcursor) shouldBe Right(obj)
  }
}
