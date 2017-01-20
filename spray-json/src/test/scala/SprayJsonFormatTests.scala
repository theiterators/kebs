import java.util.UUID

import org.scalatest.{FunSuite, Matchers}
import pl.iterators.kebs.json.KebsSpray
import spray.json._

class SprayJsonFormatTests extends FunSuite with Matchers {
  object KebsProtocol extends DefaultJsonProtocol with KebsSpray
  import KebsProtocol._

  case class C(i: Int)
  case class D(i: Int, s: String)
  case class E(noFormat: UUID)
  case object F

  case class DTO1(c: C)
  case class DTO2(c: Option[C])
  case class Compound(c: C, d: D)

  case class Parametrized1[T](field: T)
  case class Parametrized2[T0, T1](field1: T0, field2: T1)

  test("Flat format") {
    val jf = implicitly[JsonFormat[C]]
    jf.write(C(10)) shouldBe JsNumber(10)
    jf.read(JsNumber(10)) shouldBe C(10)
  }

  test("Flat format - no implicit JsonFormat") {
    "implicitly[JsonFormat[E]]" shouldNot compile
  }

  test("Flat format - parametrized") {
    val jf = implicitly[JsonFormat[Parametrized1[Double]]]
    jf.write(Parametrized1(15.0)) shouldBe JsNumber(15.0)
    jf.read(JsNumber(15.0)) shouldBe Parametrized1(15.0)
  }

  test("Root format 0") {
    val jf = implicitly[RootJsonFormat[F.type]]
    jf.write(F) shouldBe JsObject()
    jf.read(JsObject()) shouldBe F
  }

  test("Root format 1") {
    val jf = implicitly[RootJsonFormat[C]]
    jf.write(C(10)) shouldBe JsObject("i" -> JsNumber(10))
    jf.read(JsObject("i" -> JsNumber(0))) shouldBe C(0)
  }

  test("Root format 2") {
    val jf = implicitly[RootJsonFormat[D]]
    jf.write(D(10, "abcd")) shouldBe JsObject("i" -> JsNumber(10), "s" -> JsString("abcd"))
    jf.read(JsObject("i" -> JsNumber(5), "s" -> JsString("abcdef"))) shouldBe D(5, "abcdef")
  }

  test("Root format - no implicit JsonFormat") {
    "implicitly[RootJsonFormat[E]]" shouldNot compile
  }

  test("Root format - parametrized") {
    val jf = implicitly[RootJsonFormat[Parametrized2[Int, String]]]
    jf.write(Parametrized2(10, "abcd")) shouldBe JsObject("field1" -> JsNumber(10), "field2" -> JsString("abcd"))
    jf.read(JsObject("field1" -> JsNumber(5), "field2" -> JsString("abcdef"))) shouldBe Parametrized2(5, "abcdef")
  }

  test("Json format 2") {
    val jf = implicitly[JsonFormat[D]]
    jf.write(D(10, "abcd")) shouldBe JsObject("i" -> JsNumber(10), "s" -> JsString("abcd"))
    jf.read(JsObject("i" -> JsNumber(5), "s" -> JsString("abcdef"))) shouldBe D(5, "abcdef")
  }

  test("Root format - DTO style") {
    val jf = implicitly[RootJsonFormat[DTO1]]
    jf.write(DTO1(C(10))) shouldBe JsObject("c" -> JsNumber(10))
    jf.read(JsObject("c" -> JsNumber(10))) shouldBe DTO1(C(10))
  }

  test("Root format - DTO style with Option") {
    val jf = implicitly[RootJsonFormat[DTO2]]
    jf.write(DTO2(Some(C(10)))) shouldBe JsObject("c" -> JsNumber(10))
    jf.read(JsObject()) shouldBe DTO2(None)
  }

  test("Root format - compound") {
    val jf = implicitly[JsonFormat[Compound]]
    jf.write(Compound(C(5), D(10, "abcd"))) shouldBe JsObject("c" -> JsNumber(5),
                                                              "d" -> JsObject("i" -> JsNumber(10), "s" -> JsString("abcd")))
    jf.read(JsObject("c" -> JsNumber(10), "d" -> JsObject("i" -> JsNumber(100), "s" -> JsString("abb")))) shouldBe Compound(C(10),
                                                                                                                            D(100, "abb"))
  }

  case class Inner(a: Int)
  case class Wrapper(a: Inner)
  case class Holder(a: Wrapper)

  test("bug: value <none>") {
    val anything = 0
    anything match {
      case 0 => implicitly[RootJsonFormat[Holder]]
      case _ => ()
    }
  }
}
