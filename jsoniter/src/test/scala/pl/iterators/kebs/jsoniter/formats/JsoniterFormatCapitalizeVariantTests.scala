package pl.iterators.kebs.jsoniter.formats

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.jsoniter.KebsJsoniterCapitalized
import pl.iterators.kebs.jsoniter.model._
import com.github.plokhotnyuk.jsoniter_scala.core._

class JsoniterFormatCapitalizeVariantTests extends AnyFunSuite with Matchers {
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

  test("Flat format - single field case class remains unchanged") {
    val codec = implicitly[JsonValueCodec[C]]
    readFromString[C]("10")(codec) shouldBe C(10)
    writeToString[C](C(10))(codec) shouldBe "10"
  }

  test("Format 2 capitalized") {
    val codec = implicitly[JsonValueCodec[D]]
    readFromString[D]("{\"IntField\":10,\"StringField\":\"abcdef\"}")(codec) shouldBe D(10, "abcdef")
    writeToString[D](D(10, "abcdef"))(codec) shouldBe "{\"IntField\":10,\"StringField\":\"abcdef\"}"
  }

  test("Format capitalized - compound") {
    val codec = implicitly[JsonValueCodec[Compound]]
    readFromString[Compound](
      "{\"CField\":10,\"DField\":{\"IntField\":100,\"StringField\":\"abb\"}}"
    )(codec) shouldBe Compound(C(10), D(100, "abb"))

    writeToString[Compound](Compound(C(5), D(10, "abcd")))(codec) shouldBe
    "{\"CField\":5,\"DField\":{\"IntField\":10,\"StringField\":\"abcd\"}}"
  }
}
