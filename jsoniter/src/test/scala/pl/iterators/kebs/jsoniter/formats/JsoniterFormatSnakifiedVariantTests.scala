package pl.iterators.kebs.jsoniter.formats

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.jsoniter.KebsJsoniterSnakified
import pl.iterators.kebs.jsoniter.model._
import com.github.plokhotnyuk.jsoniter_scala.core._

class JsoniterFormatSnakifiedVariantTests extends AnyFunSuite with Matchers {
  object JsoniterProtocol extends KebsJsoniterSnakified with CaseClass1ToValueClass
  import JsoniterProtocol._

  test("Format 0 remains unchanged") {
    val codec = implicitly[JsonValueCodec[F.type]]
    readFromString[F.type]("{}")(codec) shouldBe F
    writeToString[F.type](F)(codec) shouldBe "{}"
  }

  test("Flat format remains unchanged") {
    val codec = implicitly[JsonValueCodec[C]]
    readFromString[C]("10")(codec) shouldBe C(10)
    writeToString[C](C(10))(codec) shouldBe "10"
  }

  test("Format 2 snakified") {
    val codec = implicitly[JsonValueCodec[D]]
    readFromString[D]("{\"int_field\":10,\"string_field\":\"abcdef\"}")(codec) shouldBe D(10, "abcdef")
    writeToString[D](D(10, "abcdef"))(codec) shouldBe "{\"int_field\":10,\"string_field\":\"abcdef\"}"
  }

  test("Format snakified - compound") {
    val codec = implicitly[JsonValueCodec[Compound]]
    readFromString[Compound](
      "{\"c_field\":10,\"d_field\":{\"int_field\":100,\"string_field\":\"abb\"}}"
    )(codec) shouldBe Compound(C(10), D(100, "abb"))

    writeToString[Compound](Compound(C(5), D(10, "abcd")))(codec) shouldBe
    "{\"c_field\":5,\"d_field\":{\"int_field\":10,\"string_field\":\"abcd\"}}"
  }

  test("Format snakified - case class with > 22 fields") {
    val codec = implicitly[JsonValueCodec[ClassWith23Fields]]
    val obj   = ClassWith23Fields.Example
    val json =
      "{\"f_1\":\"f1 value\",\"f_2\":2,\"f_3\":3,\"f_4\":null,\"f_5\":\"f5 value\",\"field_number_six\":\"six\",\"f_7\":[\"f7 value 1\",\"f7 value 2\"],\"f_8\":\"f8 value\",\"f_9\":\"f9 value\",\"f_10\":\"f10 value\",\"f_11\":\"f11 value\",\"f_12\":\"f12 value\",\"f_13\":\"f13 value\",\"f_14\":\"f14 value\",\"f_15\":\"f15 value\",\"f_16\":\"f16 value\",\"f_17\":\"f17 value\",\"f_18\":\"f18 value\",\"f_19\":\"f19 value\",\"f_20\":\"f20 value\",\"f_21\":\"f21 value\",\"f_22\":\"f22 value\",\"f_23\":true}"

    writeToString[ClassWith23Fields](obj)(codec) shouldBe json
    readFromString[ClassWith23Fields](json)(codec) shouldBe obj
  }
}
