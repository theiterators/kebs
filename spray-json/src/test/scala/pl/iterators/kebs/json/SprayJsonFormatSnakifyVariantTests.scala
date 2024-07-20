package pl.iterators.kebs.json

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import spray.json.{DefaultJsonProtocol, JsArray, JsBoolean, JsNull, JsNumber, JsObject, JsString, JsonFormat, NullOptions, RootJsonFormat}

class SprayJsonFormatSnakifyVariantTests extends AnyFunSuite with Matchers {
  import pl.iterators.kebs.core.macros.CaseClass1ToValueClass._
  object KebsProtocol extends DefaultJsonProtocol with KebsSpray.Snakified
  import KebsProtocol._

  case class C(anInteger: Int)
  case class D(intField: Int, stringField: String)
  case object F

  case class Compound(CField: C, DField: D)

  test("Flat format remains unchanged") {
    val jf = implicitly[JsonFormat[C]]
    jf.write(C(10)) shouldBe JsNumber(10)
    jf.read(JsNumber(10)) shouldBe C(10)
  }

  test("Root format 0 remains unchanged") {
    val jf = implicitly[RootJsonFormat[F.type]]
    jf.write(F) shouldBe JsObject()
    jf.read(JsObject()) shouldBe F
  }

  test("Root format 1 snakified") {
    val jf = implicitly[RootJsonFormat[C]]
    jf.write(C(10)) shouldBe JsObject("an_integer" -> JsNumber(10))
    jf.read(JsObject("an_integer" -> JsNumber(0))) shouldBe C(0)
  }

  test("Root format 2 snakified") {
    val jf = implicitly[RootJsonFormat[D]]
    jf.write(D(10, "abcd")) shouldBe JsObject("int_field" -> JsNumber(10), "string_field" -> JsString("abcd"))
    jf.read(JsObject("int_field" -> JsNumber(5), "string_field" -> JsString("abcdef"))) shouldBe D(5, "abcdef")
  }

  test("Json format 2 snakified") {
    val jf = implicitly[JsonFormat[D]]
    jf.write(D(10, "abcd")) shouldBe JsObject("int_field" -> JsNumber(10), "string_field" -> JsString("abcd"))
    jf.read(JsObject("int_field" -> JsNumber(5), "string_field" -> JsString("abcdef"))) shouldBe D(5, "abcdef")
  }

  test("Root format snakified - compound") {
    val jf = implicitly[JsonFormat[Compound]]
    jf.write(Compound(C(5), D(10, "abcd"))) shouldBe JsObject("c_field" -> JsNumber(5),
                                                              "d_field" -> JsObject("int_field" -> JsNumber(10),
                                                                                    "string_field" -> JsString("abcd")))

    jf.read(JsObject("c_field" -> JsNumber(10), "d_field" -> JsObject("int_field" -> JsNumber(100), "string_field" -> JsString("abb")))) shouldBe Compound(
      C(10),
      D(100, "abb"))
  }

  test("Root format snakified - case class with > 22 fields (issue #7)") {
    import model._

    val jf  = implicitly[JsonFormat[ClassWith23Fields]]
    val obj = ClassWith23Fields.Example
    val json = JsObject(
      Map(
        "f1"               -> JsString("f1 value"),
        "f2"               -> JsNumber(2),
        "f3"               -> JsNumber(3),
        "f5"               -> JsString("f5 value"),
        "field_number_six" -> JsString("six"),
        "f7"               -> JsArray(JsString("f7 value 1"), JsString("f7 value 2")),
        "f8"               -> JsString("f8 value"),
        "f9"               -> JsString("f9 value"),
        "f10"              -> JsString("f10 value"),
        "f11"              -> JsString("f11 value"),
        "f12"              -> JsString("f12 value"),
        "f13"              -> JsString("f13 value"),
        "f14"              -> JsString("f14 value"),
        "f15"              -> JsString("f15 value"),
        "f16"              -> JsString("f16 value"),
        "f17"              -> JsString("f17 value"),
        "f18"              -> JsString("f18 value"),
        "f19"              -> JsString("f19 value"),
        "f20"              -> JsString("f20 value"),
        "f21"              -> JsString("f21 value"),
        "f22"              -> JsString("f22 value"),
        "f23"              -> JsBoolean(true)
      ))

    jf.write(obj) shouldBe json
    jf.read(json) shouldBe obj
  }

  test("Root format snakified with NullOptions - case class with > 22 fields (issue #73)") {
    object KebsProtocolNullOptions extends DefaultJsonProtocol with KebsSpray.Snakified with NullOptions

    import KebsProtocolNullOptions._
    import model._

    val jf  = implicitly[JsonFormat[ClassWith23Fields]]
    val obj = ClassWith23Fields.Example
    val json = JsObject(
      Map(
        "f1"               -> JsString("f1 value"),
        "f2"               -> JsNumber(2),
        "f3"               -> JsNumber(3),
        "f4"               -> JsNull,
        "f5"               -> JsString("f5 value"),
        "field_number_six" -> JsString("six"),
        "f7"               -> JsArray(JsString("f7 value 1"), JsString("f7 value 2")),
        "f8"               -> JsString("f8 value"),
        "f9"               -> JsString("f9 value"),
        "f10"              -> JsString("f10 value"),
        "f11"              -> JsString("f11 value"),
        "f12"              -> JsString("f12 value"),
        "f13"              -> JsString("f13 value"),
        "f14"              -> JsString("f14 value"),
        "f15"              -> JsString("f15 value"),
        "f16"              -> JsString("f16 value"),
        "f17"              -> JsString("f17 value"),
        "f18"              -> JsString("f18 value"),
        "f19"              -> JsString("f19 value"),
        "f20"              -> JsString("f20 value"),
        "f21"              -> JsString("f21 value"),
        "f22"              -> JsString("f22 value"),
        "f23"              -> JsBoolean(true)
      ))

    jf.write(obj) shouldBe json
    jf.read(json) shouldBe obj
  }
}
