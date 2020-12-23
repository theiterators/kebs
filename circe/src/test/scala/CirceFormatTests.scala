import java.time.ZonedDateTime

import io.circe.{Decoder, Encoder, Json}
import org.scalatest.funsuite.AnyFunSuite
import pl.iterators.kebs.circe.KebsCirce
import org.scalatest.matchers.should.Matchers

class CirceFormatTests extends AnyFunSuite with Matchers {
  object KebsProtocol extends KebsCirce
  import KebsProtocol._

  case class C(i: Int)
  case class D(i: Int, s: String)
  case class E(noFormat: ZonedDateTime)
  case object F

  case class DTO1(c: C, i: Int)
  case class DTO2(c: Option[C], i: Int)
  case class Compound(c: C, d: D)

  case class Parametrized1[T](field: T)
  case class Parametrized2[T0, T1](field1: T0, field2: T1)

  case class R(a: Int, rs: Seq[R])

  test("Flat format") {
    val decoder = implicitly[Decoder[C]]
    val encoder = implicitly[Encoder[C]]
    decoder.apply(Json.fromInt(10).hcursor) shouldBe Right(C(10))
    encoder.apply(C(10)) shouldBe Json.fromInt(10)
  }

  test("Flat format - parametrized") {
    val decoder = implicitly[Decoder[Parametrized1[Double]]]
    val encoder = implicitly[Encoder[Parametrized1[Double]]]
    decoder.apply(Json.fromDouble(15.0).get.hcursor) shouldBe Right(Parametrized1(15.0))
    encoder.apply(Parametrized1(15.0)) shouldBe Json.fromDouble(15.0).get
  }

  test("Format 0") {
    val decoder = implicitly[Decoder[F.type]]
    val encoder = implicitly[Encoder[F.type]]
    decoder.apply(Json.fromFields(Seq.empty[(String, Json)]).hcursor) shouldBe Right(F)
    encoder.apply(F) shouldBe Json.fromFields(Seq.empty[(String, Json)])
  }

  test("Format 1") {
    val decoder = implicitly[Decoder[D]]
    val encoder = implicitly[Encoder[D]]
    decoder.apply(Json.fromFields(Seq("i" -> Json.fromInt(10), "s" -> Json.fromString("abcdef"))).hcursor) shouldBe Right(D(10, "abcdef"))
    encoder.apply(D(10, "abcdef")) shouldBe Json.fromFields(Seq("i" -> Json.fromInt(10), "s" -> Json.fromString("abcdef")))
  }

  test("Format - parametrized") {
    val decoder = implicitly[Decoder[Parametrized2[Int, String]]]
    val encoder = implicitly[Encoder[Parametrized2[Int, String]]]
    decoder.apply(Json.fromFields(Seq("field1" -> Json.fromInt(5), "field2" -> Json.fromString("abcdef"))).hcursor) shouldBe Right(
      Parametrized2(5, "abcdef"))
    encoder.apply(Parametrized2(5, "abcdef")) shouldBe Json.fromFields(
      Seq("field1" -> Json.fromInt(5), "field2" -> Json.fromString("abcdef")))
  }

  test("Format - DTO style") {
    val decoder = implicitly[Decoder[DTO1]]
    val encoder = implicitly[Encoder[DTO1]]
    decoder.apply(Json.fromFields(Seq("c" -> Json.fromInt(10), "i" -> Json.fromInt(5))).hcursor) shouldBe Right(DTO1(C(10), 5))
    encoder.apply(DTO1(C(10), 5)) shouldBe Json.fromFields(Seq("c" -> Json.fromInt(10), "i" -> Json.fromInt(5)))
  }

  test("Format - DTO style with Option") {
    val decoder = implicitly[Decoder[DTO2]]
    val encoder = implicitly[Encoder[DTO2]]
    decoder.apply(Json.fromFields(Seq("c" -> Json.fromInt(10), "i" -> Json.fromInt(5))).hcursor) shouldBe Right(DTO2(Some(C(10)), 5))
    encoder.apply(DTO2(None, 5)) shouldBe Json.fromFields(Seq("c" -> Json.Null, "i" -> Json.fromInt(5)))
  }

  test("Format - compound") {
    val decoder = implicitly[Decoder[Compound]]
    val encoder = implicitly[Encoder[Compound]]

    decoder.apply(
      Json
        .fromFields(Seq("c" -> Json.fromInt(10), "d" -> Json.fromFields(Seq("i" -> Json.fromInt(100), "s" -> Json.fromString("abb")))))
        .hcursor) shouldBe Right(Compound(C(10), D(100, "abb")))
    encoder.apply(Compound(C(5), D(10, "abcd"))) shouldBe Json.fromFields(
      Seq("c" -> Json.fromInt(5), "d" -> Json.fromFields(Seq("i" -> Json.fromInt(10), "s" -> Json.fromString("abcd")))))
  }

  test("Recursive format") {
    val decoder = implicitly[Decoder[R]]
    val encoder = implicitly[Encoder[R]]

    decoder.apply(
      Json
        .fromFields(Seq("a" -> Json.fromInt(1), "rs" -> Json.arr(Json.fromFields(Seq("a" -> Json.fromInt(2), "rs" -> Json.arr())))))
        .hcursor) shouldBe Right(R(1, Seq(R(2, Seq.empty[R]))))
    encoder.apply(R(1, Seq(R(2, Seq.empty[R])))) shouldBe Json.fromFields(
      Seq("a" -> Json.fromInt(1), "rs" -> Json.arr(Json.fromFields(Seq("a" -> Json.fromInt(2), "rs" -> Json.arr())))))
  }

  test("Format - case class with > 22 fields") {
    import model._

    val decoder = implicitly[Decoder[ClassWith23Fields]]
    val encoder = implicitly[Encoder[ClassWith23Fields]]
    val obj     = ClassWith23Fields.Example
    val json = Json.fromFields(
      Seq(
        "f1"             -> Json.fromString("f1 value"),
        "f2"             -> Json.fromInt(2),
        "f3"             -> Json.fromInt(3),
        "f4"             -> Json.Null,
        "f5"             -> Json.fromString("f5 value"),
        "fieldNumberSix" -> Json.fromString("six"),
        "f7"             -> Json.arr(Json.fromString("f7 value 1"), Json.fromString("f7 value 2")),
        "f8"             -> Json.fromString("f8 value"),
        "f9"             -> Json.fromString("f9 value"),
        "f10"            -> Json.fromString("f10 value"),
        "f11"            -> Json.fromString("f11 value"),
        "f12"            -> Json.fromString("f12 value"),
        "f13"            -> Json.fromString("f13 value"),
        "f14"            -> Json.fromString("f14 value"),
        "f15"            -> Json.fromString("f15 value"),
        "f16"            -> Json.fromString("f16 value"),
        "f17"            -> Json.fromString("f17 value"),
        "f18"            -> Json.fromString("f18 value"),
        "f19"            -> Json.fromString("f19 value"),
        "f20"            -> Json.fromString("f20 value"),
        "f21"            -> Json.fromString("f21 value"),
        "f22"            -> Json.fromString("f22 value"),
        "f23"            -> Json.fromBoolean(true)
      ))

    encoder.apply(obj) shouldBe json
    decoder.apply(json.hcursor) shouldBe Right(obj)
  }

  test("Nested case classes with > 22 fields") {
    import model._

    val decoder = implicitly[Decoder[ClassWith23FieldsNested]]
    val encoder = implicitly[Encoder[ClassWith23FieldsNested]]
    val obj     = ClassWith23FieldsNested.Example
    val json = Json.fromFields(
      Map(
        "f1" -> Json.fromString("f1 value"),
        "f2" -> Json.fromFields(Seq(
          "f1"             -> Json.fromString("f1 value"),
          "f2"             -> Json.fromInt(2),
          "f3"             -> Json.fromInt(3),
          "f4"             -> Json.Null,
          "f5"             -> Json.fromString("f5 value"),
          "fieldNumberSix" -> Json.fromString("six"),
          "f7"             -> Json.arr(Json.fromString("f7 value 1"), Json.fromString("f7 value 2")),
          "f8"             -> Json.fromString("f8 value"),
          "f9"             -> Json.fromString("f9 value"),
          "f10"            -> Json.fromString("f10 value"),
          "f11"            -> Json.fromString("f11 value"),
          "f12"            -> Json.fromString("f12 value"),
          "f13"            -> Json.fromString("f13 value"),
          "f14"            -> Json.fromString("f14 value"),
          "f15"            -> Json.fromString("f15 value"),
          "f16"            -> Json.fromString("f16 value"),
          "f17"            -> Json.fromString("f17 value"),
          "f18"            -> Json.fromString("f18 value"),
          "f19"            -> Json.fromString("f19 value"),
          "f20"            -> Json.fromString("f20 value"),
          "f21"            -> Json.fromString("f21 value"),
          "f22"            -> Json.fromString("f22 value"),
          "f23"            -> Json.fromBoolean(true)
        )),
        "f3"             -> Json.fromInt(3),
        "f4"             -> Json.Null,
        "f5"             -> Json.fromString("f5 value"),
        "fieldNumberSix" -> Json.fromString("six"),
        "f7"             -> Json.arr(Json.fromString("f7 value 1"), Json.fromString("f7 value 2")),
        "f8"             -> Json.fromString("f8 value"),
        "f9"             -> Json.fromString("f9 value"),
        "f10"            -> Json.fromString("f10 value"),
        "f11"            -> Json.fromString("f11 value"),
        "f12"            -> Json.fromString("f12 value"),
        "f13"            -> Json.fromString("f13 value"),
        "f14"            -> Json.fromString("f14 value"),
        "f15"            -> Json.fromString("f15 value"),
        "f16"            -> Json.fromString("f16 value"),
        "f17"            -> Json.fromString("f17 value"),
        "f18"            -> Json.fromString("f18 value"),
        "f19"            -> Json.fromString("f19 value"),
        "f20"            -> Json.fromString("f20 value"),
        "f21"            -> Json.fromString("f21 value"),
        "f22"            -> Json.fromString("f22 value"),
        "f23"            -> Json.fromBoolean(true)
      ))

    encoder.apply(obj) shouldBe json
    decoder.apply(json.hcursor) shouldBe Right(obj)
  }
}
