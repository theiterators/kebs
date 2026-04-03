package pl.iterators.kebs.sprayjson.formats

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.sprayjson.KebsSprayJson
import spray.json._

class SprayJsonFormatNoFlatTests extends AnyFunSuite with Matchers {
  object KebsProtocol extends DefaultJsonProtocol with KebsSprayJson
  import KebsProtocol._

  case class C(i: Int)
  case class D(i: Int, s: String)
  case class DTO1(c: C)
  case class Parametrized1[T](field: T)

  test("No-flat format for 1-element case class") {
    val jf = implicitly[JsonFormat[C]]
    jf.write(C(10)) shouldBe JsObject("i" -> JsNumber(10))
    jf.read(JsObject("i" -> JsNumber(10))) shouldBe C(10)
  }

  test("No-flat format - RootJsonFormat for 1-element case class") {
    val jf = implicitly[RootJsonFormat[C]]
    jf.write(C(10)) shouldBe JsObject("i" -> JsNumber(10))
    jf.read(JsObject("i" -> JsNumber(0))) shouldBe C(0)
  }

  test("No-flat format - parametrized") {
    val jf = implicitly[JsonFormat[Parametrized1[Double]]]
    jf.write(Parametrized1(15.0)) shouldBe JsObject("field" -> JsNumber(15.0))
    jf.read(JsObject("field" -> JsNumber(15.0))) shouldBe Parametrized1(15.0)
  }

  test("No-flat format - DTO style with 1-element case class field") {
    val jf = implicitly[RootJsonFormat[DTO1]]
    jf.write(DTO1(C(10))) shouldBe JsObject("c" -> JsObject("i" -> JsNumber(10)))
    jf.read(JsObject("c" -> JsObject("i" -> JsNumber(10)))) shouldBe DTO1(C(10))
  }
}
