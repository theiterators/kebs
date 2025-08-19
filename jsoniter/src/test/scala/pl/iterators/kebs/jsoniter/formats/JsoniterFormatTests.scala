package pl.iterators.kebs.jsoniter.formats

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.jsoniter.KebsJsoniter
import pl.iterators.kebs.jsoniter.ExportedCodecs._
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.jsoniter.model._
import com.github.plokhotnyuk.jsoniter_scala.core._

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import pl.iterators.kebs.jsoniter.model._

class CirceFormatTests extends AnyFunSuite with Matchers {
  object JsoniterProtocol extends KebsJsoniter with CaseClass1ToValueClass
  import JsoniterProtocol._

  test("Flat format") {
    val codec = implicitly[JsonValueCodec[C]]
    readFromString[C]("10")(codec) shouldBe C(10)
    writeToString[C](C(10))(codec) shouldBe "10"
  }

  test("Flat format - parametrized") {
    val codec = implicitly[JsonValueCodec[Parametrized1[Double]]]
    readFromString[Parametrized1[Double]]("15.0")(codec) shouldBe Parametrized1(15.0)
    writeToString[Parametrized1[Double]](Parametrized1(15.0))(codec) shouldBe "15.0"
  }

  test("Format 0") {
    val codec = implicitly[JsonValueCodec[F.type]]
    readFromString[F.type]("{}")(codec) shouldBe F
    writeToString[F.type](F)(codec) shouldBe "{}"
  }

  test("Format 1") {
    val codec = implicitly[JsonValueCodec[D]]
    readFromString[D]("{\"intField\":10,\"stringField\":\"abcdef\"}")(codec) shouldBe D(10, "abcdef")
    writeToString[D](D(10, "abcdef"))(codec) shouldBe "{\"intField\":10,\"stringField\":\"abcdef\"}"
  }

  test("Format - parametrized") {
    val codec = implicitly[JsonValueCodec[Parametrized2[Int, String]]]
    readFromString[Parametrized2[Int, String]]("{\"field1\":5,\"field2\":\"abcdef\"}")(codec) shouldBe Parametrized2(5, "abcdef")
    writeToString[Parametrized2[Int, String]](Parametrized2(5, "abcdef"))(codec) shouldBe "{\"field1\":5,\"field2\":\"abcdef\"}"
  }

  test("Format - DTO style") {
    val codec = implicitly[JsonValueCodec[DTO1]]
    readFromString[DTO1]("{\"c\":10,\"i\":5}")(codec) shouldBe DTO1(C(10), 5)
    writeToString[DTO1](DTO1(C(10), 5))(codec) shouldBe "{\"c\":10,\"i\":5}"
  }

  test("Format - DTO style with Option") {
    val codec = implicitly[JsonValueCodec[DTO2]]
    readFromString[DTO2]("{\"c\":10,\"i\":5}")(codec) shouldBe DTO2(Some(C(10)), 5)
    writeToString[DTO2](DTO2(None, 5))(codec) shouldBe "{\"c\":null,\"i\":5}"
  }

  test("Format - compound") {
    val codec = implicitly[JsonValueCodec[Compound]]

    readFromString[Compound](
      "{\"CField\":10,\"DField\":{\"intField\":100,\"stringField\":\"abb\"}}"
    )(codec) shouldBe Compound(C(10), D(100, "abb"))

    writeToString[Compound](Compound(C(5), D(10, "abcd")))(codec) shouldBe
    "{\"CField\":5,\"DField\":{\"intField\":10,\"stringField\":\"abcd\"}}"
  }

  test("Recursive format") {
    val codec = implicitly[JsonValueCodec[R]]

    readFromString[R](
      "{\"a\":1,\"rs\":[{\"a\":2,\"rs\":[]}]}"
    )(codec) shouldBe R(1, Seq(R(2, Seq.empty[R])))

    writeToString[R](R(1, Seq(R(2, Seq.empty[R]))))(codec) shouldBe
    "{\"a\":1,\"rs\":[{\"a\":2,\"rs\":[]}]}"
  }

  test("Format - case class with > 22 fields") {
    val codec = implicitly[JsonValueCodec[ClassWith23Fields]]
    val obj   = ClassWith23Fields.Example
    val json =
      "{\"f1\":\"f1 value\",\"f2\":2,\"f3\":3,\"f4\":null,\"f5\":\"f5 value\",\"fieldNumberSix\":\"six\",\"f7\":[\"f7 value 1\",\"f7 value 2\"],\"f8\":\"f8 value\",\"f9\":\"f9 value\",\"f10\":\"f10 value\",\"f11\":\"f11 value\",\"f12\":\"f12 value\",\"f13\":\"f13 value\",\"f14\":\"f14 value\",\"f15\":\"f15 value\",\"f16\":\"f16 value\",\"f17\":\"f17 value\",\"f18\":\"f18 value\",\"f19\":\"f19 value\",\"f20\":\"f20 value\",\"f21\":\"f21 value\",\"f22\":\"f22 value\",\"f23\":true}"

    writeToString[ClassWith23Fields](obj)(codec) shouldBe json
    readFromString[ClassWith23Fields](json)(codec) shouldBe obj
  }

  test("Nested case classes with > 22 fields") {
    val codec = implicitly[JsonValueCodec[ClassWith23FieldsNested]]
    val obj   = ClassWith23FieldsNested.Example
    val json =
      "{\"f1\":\"f1 value\",\"f2\":{\"f1\":\"f1 value\",\"f2\":2,\"f3\":3,\"f4\":null,\"f5\":\"f5 value\",\"fieldNumberSix\":\"six\",\"f7\":[\"f7 value 1\",\"f7 value 2\"],\"f8\":\"f8 value\",\"f9\":\"f9 value\",\"f10\":\"f10 value\",\"f11\":\"f11 value\",\"f12\":\"f12 value\",\"f13\":\"f13 value\",\"f14\":\"f14 value\",\"f15\":\"f15 value\",\"f16\":\"f16 value\",\"f17\":\"f17 value\",\"f18\":\"f18 value\",\"f19\":\"f19 value\",\"f20\":\"f20 value\",\"f21\":\"f21 value\",\"f22\":\"f22 value\",\"f23\":true},\"f3\":3,\"f4\":null,\"f5\":\"f5 value\",\"fieldNumberSix\":\"six\",\"f7\":[\"f7 value 1\",\"f7 value 2\"],\"f8\":\"f8 value\",\"f9\":\"f9 value\",\"f10\":\"f10 value\",\"f11\":\"f11 value\",\"f12\":\"f12 value\",\"f13\":\"f13 value\",\"f14\":\"f14 value\",\"f15\":\"f15 value\",\"f16\":\"f16 value\",\"f17\":\"f17 value\",\"f18\":\"f18 value\",\"f19\":\"f19 value\",\"f20\":\"f20 value\",\"f21\":\"f21 value\",\"f22\":\"f22 value\",\"f23\":true}"

    writeToString[ClassWith23FieldsNested](obj)(codec) shouldBe json
    readFromString[ClassWith23FieldsNested](json)(codec) shouldBe obj
  }
}
