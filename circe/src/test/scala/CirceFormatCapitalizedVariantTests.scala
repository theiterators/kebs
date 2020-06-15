import io.circe.{Decoder, Encoder, Json, JsonNumber}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.circe.KebsCirce

class CirceFormatCapitalizedVariantTests extends AnyFunSuite with Matchers {
  object KebsProtocol extends KebsCirce with KebsCirce.Capitalized
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

  test("Format 1 capitalized") {
    val decoder = implicitly[Decoder[C]]
    val encoder = implicitly[Encoder[C]]
    decoder.apply(Json.fromFields(Seq("AnInteger" -> Json.fromInt(10))).hcursor).right.get shouldBe C(10)
    encoder.apply(C(10)) shouldBe Json.fromFields(Seq("AnInteger" -> Json.fromInt(10)))
  }

  test("Format 2 capitalized") {
    val decoder = implicitly[Decoder[D]]
    val encoder = implicitly[Encoder[D]]
    decoder
      .apply(Json.fromFields(Seq("IntField" -> Json.fromInt(5), "StringField" -> Json.fromString("abcd"))).hcursor)
      .right
      .get shouldBe D(5, "abcd")
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
          .hcursor)
      .right
      .get shouldBe Compound(C(5), D(10, "abcd"))
  }
}
