import akka.http.scaladsl.server.directives.ParameterDirectives._
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.Unmarshal
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FunSuite, Matchers}

class AkkaHttpUnmarshallerTests extends FunSuite with Matchers with ScalatestRouteTest with ScalaFutures {
  case class I(i: Int)
  case class S(s: String)
  case class P[A](a: A)
  case class CantUnmarshall(s: String, i: Int)
  case object O

  import pl.iterators.kebs.unmarshallers._

  test("Unmarhsal") {
    Unmarshal(42).to[I].futureValue shouldBe I(42)
    Unmarshal("42").to[S].futureValue shouldBe S("42")
  }

  test("Unmarhsal parametrized") {
    Unmarshal("42").to[P[String]].futureValue shouldBe P("42")
  }

  test("Unmarhsal case object") {
    Unmarshal("42").to[O.type].futureValue shouldBe O
    Unmarshal(42).to[O.type].futureValue shouldBe O
  }

  test("No unmarshaller for case-classes of arity > 1") {
    """Unmarshal("42").to[CantUnmarshall]""" shouldNot compile
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

  test("Unmarshalling repeated parameter") {
    val testRoute = parameters('i.as[I].?) { i =>
      complete(i.toString)
    }
    Get("/?i=42") ~> testRoute ~> check {
      responseAs[String] shouldEqual "Some(I(42))"
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
