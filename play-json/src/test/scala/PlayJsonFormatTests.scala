import java.util.UUID

import org.scalatest.{FunSuite, Matchers}
import play.api.libs.json._

class PlayJsonFormatTests extends FunSuite with Matchers {
  import pl.iterators.kebs.json._

  case class C(i: Int)
  case class D(s: String)
  case class E(noFormat: UUID)

  case class Parametrized[T](field: T)

  case class DTO(c: C, d: D)

  test("Flat format") {
    val jf = implicitly[Format[C]]
    jf.writes(C(10)) shouldBe JsNumber(10)
    jf.reads(JsNumber(10)) shouldBe JsSuccess(C(10))
  }

  test("Flat format - no implicit JsonFormat") {
    "implicitly[JsonFormat[E]]" shouldNot compile
  }

  test("Flat format - parametrized") {
    val jf = implicitly[Format[Parametrized[Double]]]
    jf.writes(Parametrized(15.0)) shouldBe JsNumber(15.0)
    jf.reads(JsNumber(15.0)) shouldBe JsSuccess(Parametrized(15.0))
  }

  test("Reads only") {
    val jf = implicitly[Reads[C]]
    jf.reads(JsNumber(10)) shouldBe JsSuccess(C(10))
  }

  test("Writes only") {
    val jf = implicitly[Writes[C]]
    jf.writes(C(10)) shouldBe JsNumber(10)
  }

  test("with Json.format") {
    val jf = Json.format[DTO]
    jf.writes(DTO(C(50), D("a"))) shouldBe Json.obj("c" -> JsNumber(50), "d" -> JsString("a"))
    jf.reads(Json.obj("c" -> JsNumber(50), "d" -> JsString("a"))) shouldBe JsSuccess(DTO(C(50), D("a")))
  }

}
