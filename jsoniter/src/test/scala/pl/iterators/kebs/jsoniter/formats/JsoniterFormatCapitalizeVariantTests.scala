package pl.iterators.kebs.jsoniter.formats

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.jsoniter.KebsJsoniterCapitalized
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.jsoniter.model._
import com.github.plokhotnyuk.jsoniter_scala.core._
import pl.iterators.kebs.jsoniter.model._

class JsoniterFormatCapitalizedVariantTests extends AnyFunSuite with Matchers {
  object JsoniterProtocol extends KebsJsoniterCapitalized with CaseClass1ToValueClass
  import JsoniterProtocol._

  test("Flat format remains unchanged") {
    val codec = implicitly[JsonValueCodec[C]]
    readFromString[C]("10")(codec) shouldBe C(10)
    writeToString[C](C(10))(codec) shouldBe "10"
  }

  test("Format 0 remains unchanged") {
    val codec = implicitly[JsonValueCodec[F.type]]
    readFromString[F.type]("{}")(codec) shouldBe F
    writeToString[F.type](F)(codec) shouldBe "{}"
  }

  test("Format 2 capitalized") {
    val codec = implicitly[JsonValueCodec[D]]
    readFromString[D]("{\"IntField\":5,\"StringField\":\"abcd\"}")(codec) shouldBe D(5, "abcd")
    writeToString[D](D(5, "abcd"))(codec) shouldBe "{\"IntField\":5,\"StringField\":\"abcd\"}"
  }

  test("Format capitalized - compound") {
    val codec = implicitly[JsonValueCodec[Compound]]
    writeToString[Compound](Compound(C(5), D(10, "abcd")))(codec) shouldBe
    "{\"CField\":5,\"DField\":{\"IntField\":10,\"StringField\":\"abcd\"}}"

    readFromString[Compound](
      "{\"CField\":5,\"DField\":{\"IntField\":10,\"StringField\":\"abcd\"}}"
    )(codec) shouldBe Compound(C(5), D(10, "abcd"))
  }

  test("Format capitalized - case class with > 22 fields") {
    val codec = implicitly[JsonValueCodec[ClassWith23Fields]]
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
    val json =
      "{\"F1\":\"f1 value\",\"F2\":2,\"F3\":3,\"F4\":null,\"F5\":\"f5 value\",\"FieldNumberSix\":\"six\",\"F7\":[\"f7 value 1\",\"f7 value 2\"],\"F8\":\"f8 value\",\"F9\":\"f9 value\",\"F10\":\"f10 value\",\"F11\":\"f11 value\",\"F12\":\"f12 value\",\"F13\":\"f13 value\",\"F14\":\"f14 value\",\"F15\":\"f15 value\",\"F16\":\"f16 value\",\"F17\":\"f17 value\",\"F18\":\"f18 value\",\"F19\":\"f19 value\",\"F20\":\"f20 value\",\"F21\":\"f21 value\",\"F22\":\"f22 value\",\"F23\":true}"

    writeToString[ClassWith23Fields](obj)(codec) shouldBe json
    readFromString[ClassWith23Fields](json)(codec) shouldBe obj
  }

  test("Additional test") {
    val codec = implicitly[JsonValueCodec[C]]
    readFromString[C]("10")(codec) shouldBe C(10)
    writeToString[C](C(10))(codec) shouldBe "10"
  }
}
