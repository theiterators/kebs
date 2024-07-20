package pl.iterators.kebs.circe

import io.circe.{Decoder, Encoder, Json, JsonNumber}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.circe.KebsCirce
import io.circe.derivation.Configuration
import io.circe.derivation.ConfiguredDecoder
import pl.iterators.kebs.circe.model._

class CirceFormatCapitalizedVariantTests extends AnyFunSuite with Matchers {
  object KebsProtocol extends KebsCirce with KebsCirce.Capitalized
  import KebsProtocol._
  import pl.iterators.kebs.core.macros.CaseClass1ToValueClass._


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

  test("Format 2 capitalized") {
    val decoder = implicitly[Decoder[D]]
    val encoder = implicitly[Encoder[D]]
    decoder
      .apply(Json.fromFields(Seq("IntField" -> Json.fromInt(5), "StringField" -> Json.fromString("abcd"))).hcursor) shouldBe Right(
      D(5, "abcd"))
    encoder.apply(D(5, "abcd")) shouldBe Json.fromFields(Seq("IntField" -> Json.fromInt(5), "StringField" -> Json.fromString("abcd")))
  }

  test("Format capitalized - compound") {
    val decoder = implicitly[Decoder[Compound]]
    val encoder = implicitly[Encoder[Compound]]

    encoder.apply(Compound(C(5), D(10, "abcd"))) shouldBe Json.fromFields(
      Seq("CField" -> Json.fromInt(5),
          "DField" -> Json.fromFields(Seq("IntField" -> Json.fromInt(10), "StringField" -> Json.fromString("abcd")))))
    decoder
      .apply(
        Json
          .fromFields(
            Seq(
              "CField" -> Json.fromInt(5),
              "DField" -> Json.fromFields(Seq("IntField" -> Json.fromInt(10), "StringField" -> Json.fromString("abcd")))
            )
          )
          .hcursor) shouldBe Right(Compound(C(5), D(10, "abcd")))
  }

  test("Format capitalized - case class with > 22 fields") {
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
        "F1"             -> Json.fromString("f1 value"),
        "F2"             -> Json.fromInt(2),
        "F3"             -> Json.fromInt(3),
        "F4"             -> Json.Null,
        "F5"             -> Json.fromString("f5 value"),
        "FieldNumberSix" -> Json.fromString("six"),
        "F7"             -> Json.arr(Json.fromString("f7 value 1"), Json.fromString("f7 value 2")),
        "F8"             -> Json.fromString("f8 value"),
        "F9"             -> Json.fromString("f9 value"),
        "F10"            -> Json.fromString("f10 value"),
        "F11"            -> Json.fromString("f11 value"),
        "F12"            -> Json.fromString("f12 value"),
        "F13"            -> Json.fromString("f13 value"),
        "F14"            -> Json.fromString("f14 value"),
        "F15"            -> Json.fromString("f15 value"),
        "F16"            -> Json.fromString("f16 value"),
        "F17"            -> Json.fromString("f17 value"),
        "F18"            -> Json.fromString("f18 value"),
        "F19"            -> Json.fromString("f19 value"),
        "F20"            -> Json.fromString("f20 value"),
        "F21"            -> Json.fromString("f21 value"),
        "F22"            -> Json.fromString("f22 value"),
        "F23"            -> Json.fromBoolean(true)
      ))

    encoder.apply(obj) shouldBe json
    decoder.apply(json.hcursor) shouldBe Right(obj)
  }
}
