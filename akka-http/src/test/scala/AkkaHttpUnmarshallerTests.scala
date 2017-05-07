import akka.http.scaladsl.server.MalformedQueryParamRejection
import akka.http.scaladsl.server.directives.ParameterDirectives._
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.Unmarshal
import enumeratum._
import enumeratum.values.{IntEnum, IntEnumEntry}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FunSuite, Matchers}

class AkkaHttpUnmarshallerTests extends FunSuite with Matchers with ScalatestRouteTest with ScalaFutures {
  case class I(i: Int)
  case class S(s: String)
  case class P[A](a: A)
  case class CantUnmarshall(s: String, i: Int)
  case object O

  sealed trait Greeting extends EnumEntry

  object Greeting extends Enum[Greeting] {
    val values = findValues

    case object Hello   extends Greeting
    case object GoodBye extends Greeting
    case object Hi      extends Greeting
    case object Bye     extends Greeting
  }

  sealed abstract class LibraryItem(val value: Int) extends IntEnumEntry

  object LibraryItem extends IntEnum[LibraryItem] {
    case object Book     extends LibraryItem(1)
    case object Movie    extends LibraryItem(2)
    case object Magazine extends LibraryItem(3)
    case object CD       extends LibraryItem(4)

    val values = findValues
  }

  import pl.iterators.kebs.unmarshallers._
  import enums._

  test("Unmarshal") {
    Unmarshal(42).to[I].futureValue shouldBe I(42)
    Unmarshal("42").to[S].futureValue shouldBe S("42")
  }

  test("Unmarshal parametrized") {
    Unmarshal("42").to[P[String]].futureValue shouldBe P("42")
  }

  test("Unmarshal case object") {
    Unmarshal("42").to[O.type].futureValue shouldBe O
    Unmarshal(42).to[O.type].futureValue shouldBe O
  }

  test("Unmarshal enum") {
    Unmarshal("hello").to[Greeting].futureValue shouldBe Greeting.Hello
    Unmarshal("blah").to[Greeting].failed.futureValue shouldBe a[IllegalArgumentException]
  }

  test("Unmarshal value enum") {
    Unmarshal(3).to[LibraryItem].futureValue shouldBe LibraryItem.Magazine
    Unmarshal(5).to[LibraryItem].failed.futureValue shouldBe a[IllegalArgumentException]
  }

  test("No unmarshaller for case-classes of arity > 1") {
    """Unmarshal("42").to[CantUnmarshall]""" shouldNot compile
  }

  test("Unmarshalling value enums is type-safe") {
    """Unmarshal(1L).to[LibraryItem]""" shouldNot compile
  }

  test("Unmarshal from string") {
    Unmarshal("42").to[I].futureValue shouldBe I(42)
  }

  test("Unmarshalling parameter") {
    val testRoute = parameters('i.as[I]) { i =>
      complete(i.toString)
    }
    Get("/?i=42") ~> testRoute ~> check {
      responseAs[String] shouldEqual "I(42)"
    }
  }

  test("Unmarshalling optional parameter") {
    val testRoute = parameters('i.as[I].?) { i =>
      complete(i.toString)
    }
    Get("/?i=42") ~> testRoute ~> check {
      responseAs[String] shouldEqual "Some(I(42))"
    }
  }

  test("Unmarshalling enum parameter") {
    val testRoute = parameters('greeting.as[Greeting]) { greeting =>
      complete(greeting.toString)
    }
    Get("/?greeting=hi") ~> testRoute ~> check {
      responseAs[String] shouldEqual "Hi"
    }
    Get("/?greeting=blah") ~> testRoute ~> check {
      rejection shouldEqual MalformedQueryParamRejection("greeting",
                                                         "Invalid value 'blah'. Expected one of: Hello, GoodBye, Hi, Bye",
                                                         None)
    }
  }

  case class Red(value: Int)
  case class Green(value: Int)
  case class Blue(value: Int)
  case class Color(red: Red, green: Green, blue: Blue)

  test("Case class extraction") {
    val route =
      path("color") {
        parameters('red.as[Red], 'green.as[Green], 'blue.as[Blue]).as(Color) { color =>
          complete(color.toString)
        }
      }
    Get("/color?red=1&green=2&blue=3") ~> route ~> check { responseAs[String] shouldEqual "Color(Red(1),Green(2),Blue(3))" }

  }
}
