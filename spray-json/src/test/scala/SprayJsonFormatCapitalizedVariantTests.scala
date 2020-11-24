import pl.iterators.kebs.json.KebsSpray
import spray.json.{DefaultJsonProtocol, JsArray, JsBoolean, JsNull, JsNumber, JsObject, JsString, JsonFormat, RootJsonFormat}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class SprayJsonFormatCapitalizedVariantTests extends AnyFunSuite with Matchers {
  object KebsProtocol extends DefaultJsonProtocol with KebsSpray.Capitalized
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

  test("Root format 1 capitalized") {
    val jf = implicitly[RootJsonFormat[C]]
    jf.write(C(10)) shouldBe JsObject("AnInteger" -> JsNumber(10))
    jf.read(JsObject("AnInteger" -> JsNumber(0))) shouldBe C(0)
  }

  test("Root format 2 capitalized") {
    val jf = implicitly[RootJsonFormat[D]]
    jf.write(D(10, "abcd")) shouldBe JsObject("IntField" -> JsNumber(10), "StringField" -> JsString("abcd"))
    jf.read(JsObject("IntField" -> JsNumber(5), "StringField" -> JsString("abcdef"))) shouldBe D(5, "abcdef")
  }

  test("Json format 2 capitalized") {
    val jf = implicitly[JsonFormat[D]]
    jf.write(D(10, "abcd")) shouldBe JsObject("IntField" -> JsNumber(10), "StringField" -> JsString("abcd"))
    jf.read(JsObject("IntField" -> JsNumber(5), "StringField" -> JsString("abcdef"))) shouldBe D(5, "abcdef")
  }

  test("Root format capitalized - compound") {
    val jf = implicitly[JsonFormat[Compound]]
    jf.write(Compound(C(5), D(10, "abcd"))) shouldBe JsObject("CField" -> JsNumber(5),
                                                              "DField" -> JsObject("IntField" -> JsNumber(10),
                                                                                   "StringField" -> JsString("abcd")))

    jf.read(JsObject("CField" -> JsNumber(10), "DField" -> JsObject("IntField" -> JsNumber(100), "StringField" -> JsString("abb")))) shouldBe Compound(
      C(10),
      D(100, "abb"))
  }
}
