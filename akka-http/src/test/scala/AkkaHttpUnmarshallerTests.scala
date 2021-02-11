import akka.http.scaladsl.server.MalformedQueryParamRejection
import akka.http.scaladsl.server.directives.ParameterDirectives._
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.Unmarshal
import enumeratum._
import enumeratum.values._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class AkkaHttpUnmarshallerTests extends AnyFunSuite with Matchers with ScalatestRouteTest with ScalaFutures {
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
    """Unmarshal(42).to[O.type]""" shouldNot compile
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
    val testRoute = parameters(Symbol("i").as[I]) { i =>
      complete(i.toString)
    }
    Get("/?i=42") ~> testRoute ~> check {
      responseAs[String] shouldEqual "I(42)"
    }
  }

  test("Unmarshalling optional parameter") {
    val testRoute = parameters(Symbol("i").as[I].?) { i =>
      complete(i.toString)
    }
    Get("/?i=42") ~> testRoute ~> check {
      responseAs[String] shouldEqual "Some(I(42))"
    }
  }

  test("Unmarshalling enum parameter") {
    val testRoute = parameters(Symbol("greeting").as[Greeting]) { greeting =>
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

  test("Unmarshalling value enum parameter") {
    val testRoute = parameters(Symbol("libraryItem").as[LibraryItem]) { item =>
      complete(item.toString)
    }
    Get("/?libraryItem=1") ~> testRoute ~> check {
      responseAs[String] shouldEqual "Book"
    }
    Get("/?libraryItem=10") ~> testRoute ~> check {
      rejection shouldEqual MalformedQueryParamRejection("libraryItem", "Invalid value '10'. Expected one of: 1, 2, 3, 4", None)
    }
  }

  case class Red(value: Int)
  case class Green(value: Int)
  case class Blue(value: Int)
  case class Color(red: Red, green: Green, blue: Blue)

  test("Case class extraction") {
    val route =
      path("color") {
        parameters(Symbol("red").as[Red], Symbol("green").as[Green], Symbol("blue").as[Blue]).as(Color) { color =>
          complete(color.toString)
        }
      }
    Get("/color?red=1&green=2&blue=3") ~> route ~> check { responseAs[String] shouldEqual "Color(Red(1),Green(2),Blue(3))" }

  }

  sealed abstract class ShirtSize(val value: String) extends StringEnumEntry

  object ShirtSize extends StringEnum[ShirtSize] {

    case object Small  extends ShirtSize("S")
    case object Medium extends ShirtSize("M")
    case object Large  extends ShirtSize("L")

    val values = findValues

  }

  test("Unmarshalling string value enum parameter") {
    val testRoute = parameters(Symbol("shirtSize").as[ShirtSize]) { shirtSize =>
      complete(shirtSize.toString)
    }
    Get("/?shirtSize=M") ~> testRoute ~> check {
      responseAs[String] shouldEqual "Medium"
    }
    Get("/?shirtSize=XL") ~> testRoute ~> check {
      rejection shouldEqual MalformedQueryParamRejection("shirtSize", "Invalid value 'XL'. Expected one of: S, M, L", None)
    }
  }

  sealed trait SortOrder extends EnumEntry
  object SortOrder extends Enum[SortOrder] {
    case object Asc  extends SortOrder
    case object Desc extends SortOrder

    override val values = findValues
  }

  test("bug: work with default enum values") {
    val route =
      path("test_enum") {
        parameter(Symbol("sort").as[SortOrder] ? (SortOrder.Desc: SortOrder)) { sort =>
          complete {
            s"Sort was $sort"
          }
        }
      }

    Get("/test_enum?sort=Asc") ~> route ~> check {
      responseAs[String] shouldBe "Sort was Asc"
    }
    Get("/test_enum") ~> route ~> check {
      responseAs[String] shouldBe "Sort was Desc"
    }

  }
}
