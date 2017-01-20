import enumeratum.{Enum, EnumEntry}
import org.scalatest.{FunSuite, Matchers}
import play.api.libs.json._

class PlayEnumJsonFormatTests extends FunSuite with Matchers {
  sealed trait Greeting extends EnumEntry

  object Greeting extends Enum[Greeting] {
    val values = findValues

    case object Hello   extends Greeting
    case object GoodBye extends Greeting
    case object Hi      extends Greeting
    case object Bye     extends Greeting
  }

  import Greeting._

  test("enum Format") {
    import pl.iterators.kebs.json.enums._

    val jf = implicitly[Format[Greeting]]
    jf.reads(JsString("hello")) shouldBe JsSuccess(Hello)
    jf.reads(JsString("goodbye")) shouldBe JsSuccess(GoodBye)
    jf.writes(Hello) shouldBe JsString("Hello")
    jf.writes(GoodBye) shouldBe JsString("GoodBye")
  }

  test("error.expected.validenumvalue") {
    import pl.iterators.kebs.json.enums._

    val jf = implicitly[Format[Greeting]]
    jf.reads(JsString("xxx")) shouldBe JsError("error.expected.validenumvalue")
  }

  test("error.expected.enumstring") {
    import pl.iterators.kebs.json.enums._

    val jf = implicitly[Format[Greeting]]
    jf.reads(JsBoolean(true)) shouldBe JsError("error.expected.enumstring")
  }

  test("enum Format - lowercase") {
    import pl.iterators.kebs.json.enums.lowercase._

    val jf = implicitly[Format[Greeting]]
    jf.reads(JsString("hello")) shouldBe JsSuccess(Hello)
    jf.reads(JsString("goodbye")) shouldBe JsSuccess(GoodBye)
    jf.writes(Hello) shouldBe JsString("hello")
    jf.writes(GoodBye) shouldBe JsString("goodbye")
  }

  test("enum Format - uppercase") {
    import pl.iterators.kebs.json.enums.uppercase._

    val jf = implicitly[Format[Greeting]]
    jf.reads(JsString("HELLO")) shouldBe JsSuccess(Hello)
    jf.reads(JsString("GOODBYE")) shouldBe JsSuccess(GoodBye)
    jf.writes(Hello) shouldBe JsString("HELLO")
    jf.writes(GoodBye) shouldBe JsString("GOODBYE")
  }
}
