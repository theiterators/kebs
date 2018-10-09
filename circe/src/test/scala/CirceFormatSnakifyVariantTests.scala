import io.circe.{Decoder, Encoder, Json}
import org.scalatest.{FunSuite, Matchers}
import pl.iterators.kebs.circe.KebsCirce

class CirceFormatSnakifyVariantTests extends FunSuite with Matchers {
  object KebsProtocol extends KebsCirce with KebsCirce.Snakified
  import KebsProtocol._

  case class C(anInteger: Int)
  case class D(intField: Int, stringField: String)
  case object F

  case class Compound(CField: C, DField: D)

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

  test("Format 2 snakified") {
    val decoder = implicitly[Decoder[D]]
    val encoder = implicitly[Encoder[D]]
    decoder.apply(Json.fromFields(Seq("int_field" -> Json.fromInt(10), "string_field" -> Json.fromString("abcdef"))).hcursor) shouldBe Right(
      D(10, "abcdef"))
    encoder.apply(D(10, "abcdef")) shouldBe Json.fromFields(
      Seq("int_field" -> Json.fromInt(10), "string_field" -> Json.fromString("abcdef")))
  }

  test("Format snakified - compound") {
    val decoder = implicitly[Decoder[Compound]]
    val encoder = implicitly[Encoder[Compound]]

    decoder.apply(
      Json
        .fromFields(Seq("c_field" -> Json.fromInt(10),
                        "d_field" -> Json.fromFields(Seq("int_field" -> Json.fromInt(100), "string_field" -> Json.fromString("abb")))))
        .hcursor) shouldBe Right(Compound(C(10), D(100, "abb")))
    encoder.apply(Compound(C(5), D(10, "abcd"))) shouldBe Json.fromFields(
      Seq("c_field" -> Json.fromInt(5),
          "d_field" -> Json.fromFields(Seq("int_field" -> Json.fromInt(10), "string_field" -> Json.fromString("abcd")))))
  }

  test("Format snakified - case class with > 22 fields (issue #7)") {
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
      true
    )
    val json = Json.fromFields(
      Map(
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
      ))

    encoder.apply(obj) shouldBe json
    decoder.apply(json.hcursor) shouldBe Right(obj)
  }
}
