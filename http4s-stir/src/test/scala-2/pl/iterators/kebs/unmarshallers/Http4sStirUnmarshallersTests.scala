package pl.iterators.kebs.unmarshallers

import org.http4s.UrlForm
import pl.iterators.stir.server.{Directives, MalformedQueryParamRejection}
import pl.iterators.stir.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.Domain._
import pl.iterators.kebs.instances.net.URIString
import pl.iterators.kebs.instances.time.{DayOfWeekInt, YearMonthString}
import pl.iterators.kebs.unmarshallers.enums.KebsEnumUnmarshallers

import java.time.{DayOfWeek, YearMonth}

class Http4sStirUnmarshallersTests
    extends AnyFunSuite
    with Matchers
    with ScalatestRouteTest
    with ScalaFutures
    with Directives
    with KebsUnmarshallers
    with KebsEnumUnmarshallers
    with URIString
    with YearMonthString
    with DayOfWeekInt {
      implicit def runtime: cats.effect.unsafe.IORuntime = cats.effect.unsafe.IORuntime.global

  test("No ValueClassLike implicits derived") {
    import pl.iterators.kebs.macros.ValueClassLike

    "implicitly[ValueClassLike[URI, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, URI]]" shouldNot typeCheck
    "implicitly[ValueClassLike[YearMonth, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, YearMonth]]" shouldNot typeCheck
    "implicitly[ValueClassLike[DayOfWeek, Int]]" shouldNot typeCheck
    "implicitly[ValueClassLike[Int, DayOfWeek]]" shouldNot typeCheck
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

  test("Case class extraction") {
    val route =
      path("color") {
        parameters(Symbol("red").as[Red], Symbol("green").as[Green], Symbol("blue").as[Blue]).as(Color) { color =>
          complete(color.toString)
        }
      }
    Get("/color?red=1&green=2&blue=3") ~> route ~> check { responseAs[String] shouldEqual "Color(Red(1),Green(2),Blue(3))" }
  }

  test("Unmarshalling instances parameter") {
    val testRoute = path("instances") {
      parameters(Symbol("year").as[YearMonth]) { year =>
        complete(year.toString)
      }
    }
    Get("/instances?year=2021-05") ~> testRoute ~> check {
      responseAs[String] shouldEqual "2021-05"
    }
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

  test("Unmarshal form field from String") {
    val route =
      path("test_form_fields") {
        formFields("yearMonth".as[YearMonth]) { yearMonth =>
          complete(yearMonth.toString)
        }
      }

    Post("/test_form_fields", UrlForm("yearMonth" -> "2021-05")) ~> route ~> check {
      responseAs[String] shouldEqual "2021-05"
    }
  }

  test("Unmarshal form fields from Int") {
    val route =
      path("test_form_fields") {
        formFields("dayOfWeek".as[DayOfWeek]) { dayOfWeek =>
          complete(dayOfWeek.getValue.toString)
        }
      }

    Post("/test_form_fields", UrlForm("dayOfWeek" -> "1")) ~> route ~> check {
      responseAs[String] shouldEqual "1"
    }
  }

  test("Unmarshal tagged parameters from Long") {
    val route =
      path("test_tagged") {
        parameter("tagged".as[Id]) { id =>
          complete(id.toString)
        }
      }

    Get("/test_tagged?tagged=123456") ~> route ~> check {
      responseAs[String] shouldBe "123456"
    }
  }

  test("Unmarshal tagged parameters from UUID") {
    val route =
      path("test_tagged") {
        parameter("tagged".as[TestId]) { id =>
          complete(id.toString)
        }
      }

    Get("/test_tagged?tagged=ce7a7cf1-8c00-49a9-a963-9fd119dd0642") ~> route ~> check {
      responseAs[String] shouldBe "ce7a7cf1-8c00-49a9-a963-9fd119dd0642"
    }
  }

  test("Unmarshal tagged URI") {
    val route =
      path("test_tagged") {
        parameter("tagged".as[TestTaggedUri]) { id =>
          complete(id.toString)
        }
      }

    Get("/test_tagged?tagged=www.test.pl") ~> route ~> check {
      responseAs[String] shouldBe "www.test.pl"
    }
  }
}
