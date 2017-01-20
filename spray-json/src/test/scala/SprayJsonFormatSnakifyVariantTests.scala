import org.scalatest.{FunSuite, Matchers}
import pl.iterators.kebs.json.KebsSpray
import spray.json.{DefaultJsonProtocol, JsNumber, JsObject, JsString, JsonFormat, RootJsonFormat}

class SprayJsonFormatSnakifyVariantTests extends FunSuite with Matchers {
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
}
