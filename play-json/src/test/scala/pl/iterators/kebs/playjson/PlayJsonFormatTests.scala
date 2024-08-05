package pl.iterators.kebs.playjson

import enumeratum.{Enum, EnumEntry}
import enumeratum.values.{LongEnum, LongEnumEntry}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.enums.ValueEnumLikeEntry
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.enumeratum.{KebsEnumeratum, KebsValueEnumeratum}
import play.api.libs.json._

import java.util.UUID

class PlayJsonFormatTests extends AnyFunSuite with Matchers with CaseClass1ToValueClass with KebsEnumeratum with KebsValueEnumeratum {

  case class C(i: Int)
  case class D(s: String)
  case class E(noFormat: UUID)

  case class Parametrized[T](field: T)

  case class DTO(c: C, d: D)

  sealed abstract class LongGreeting(val value: Long) extends LongEnumEntry with ValueEnumLikeEntry[Long]

  object LongGreeting extends LongEnum[LongGreeting] {
    val values = findValues

    case object Hello   extends LongGreeting(0L)
    case object GoodBye extends LongGreeting(1L)
    case object Hi      extends LongGreeting(2L)
    case object Bye     extends LongGreeting(3L)
  }

  sealed trait Greeting extends EnumEntry

  object Greeting extends Enum[Greeting] {
    val values = findValues

    case object Hello   extends Greeting
    case object GoodBye extends Greeting
    case object Hi      extends Greeting
    case object Bye     extends Greeting
  }

  test("Flat format") {
    val jf = implicitly[Format[C]]
    jf.writes(C(10)) shouldBe JsNumber(10)
    jf.reads(JsNumber(10)) shouldBe JsSuccess(C(10))
  }

  test("Flat format - no implicit JsonFormat") {
    "implicitly[JsonFormat[E]]" shouldNot typeCheck
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

  import enums._

  test("enum JsonFormat") {
    import Greeting._
    val decoder = implicitly[Reads[Greeting]]
    val encoder = implicitly[Writes[Greeting]]
    decoder.reads(JsString("hElLo")) shouldBe JsSuccess(Hello)
    decoder.reads(JsString("goodbye")) shouldBe JsSuccess(GoodBye)
    encoder.writes(Hello) shouldBe JsString("Hello")
    encoder.writes(GoodBye) shouldBe JsString("GoodBye")
  }

  test("enum name deserialization error") {
    val decoder = implicitly[Reads[Greeting]]
    decoder.reads(JsNumber(BigDecimal(1))) shouldBe a[JsError]
  }

  test("enum JsonFormat - lowercase") {
    import Greeting._
    val decoder = implicitly[Reads[Greeting]]
    val encoder = implicitly[Writes[Greeting]]
    decoder.reads(JsString("hello")) shouldBe JsSuccess(Hello)
    decoder.reads(JsString("goodbye")) shouldBe JsSuccess(GoodBye)
    encoder.writes(Hello) shouldBe JsString("Hello")
    encoder.writes(GoodBye) shouldBe JsString("GoodBye")
  }

  test("enum JsonFormat - uppercase") {
    import Greeting._
    val decoder = implicitly[Reads[Greeting]]
    val encoder = implicitly[Writes[Greeting]]
    decoder.reads(JsString("HELLO")) shouldBe JsSuccess(Hello)
    decoder.reads(JsString("GOODBYE")) shouldBe JsSuccess(GoodBye)
    encoder.writes(Hello) shouldBe JsString("Hello")
    encoder.writes(GoodBye) shouldBe JsString("GoodBye")
  }

  test("value enum JsonFormat") {
    import LongGreeting._
    val decoder = implicitly[Reads[LongGreeting]]
    val encoder = implicitly[Writes[LongGreeting]]
    decoder.reads(JsNumber(0)) shouldBe JsSuccess(Hello)
    decoder.reads(JsNumber(1)) shouldBe JsSuccess(GoodBye)
    encoder.writes(Hello) shouldBe JsNumber(0)
    encoder.writes(GoodBye) shouldBe JsNumber(1)
  }

  test("value enum deserialization error") {
    import LongGreeting._
    val decoder = implicitly[Reads[LongGreeting]]
    decoder.reads(JsNumber(4)) shouldBe a[JsError]
  }

}
