package pl.iterators.kebs.http4sstir.matchers

import org.http4s.Status
import pl.iterators.stir.server.{Directives, Route}
import pl.iterators.stir.testkit.{RouteTestTimeout, ScalatestRouteTest}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.circe.KebsEnumForTests
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.http4sstir.KebsValueEnumForTests
import pl.iterators.kebs.instances.net.URIString
import pl.iterators.kebs.instances.time.{DayOfWeekInt, ZonedDateTimeString}
import pl.iterators.kebs.instances.time.mixins.InstantEpochMilliLong
import pl.iterators.kebs.http4sstir.domain.Domain._

import scala.concurrent.duration._
import java.net.URI
import java.time.{DayOfWeek, Instant, ZonedDateTime}

object Http4sStirMatchersTests {
  final case class Positive(value: Int) { require(value > 0, "not positive") }
}

class Http4sStirMatchersTests
    extends AnyFunSuite
    with Matchers
    with Directives
    with ScalatestRouteTest
    with ScalaFutures
    with ZonedDateTimeString
    with DayOfWeekInt
    with InstantEpochMilliLong
    with URIString
    with KebsEnumForTests
    with KebsValueEnumForTests
    with CaseClass1ToValueClass {
  // the first request of a suite can take over the 1s default on a cold CI JVM
  implicit val routeTestTimeout: RouteTestTimeout = RouteTestTimeout(5.seconds)

  implicit def runtime: cats.effect.unsafe.IORuntime = cats.effect.unsafe.IORuntime.global

  test("No ValueClassLike implicits derived") {
    import pl.iterators.kebs.core.macros.ValueClassLike

    "implicitly[ValueClassLike[DayOfWeek, Int]]" shouldNot typeCheck
    "implicitly[ValueClassLike[Int, DayOfWeek]]" shouldNot typeCheck
    "implicitly[ValueClassLike[Instant, Long]]" shouldNot typeCheck
    "implicitly[ValueClassLike[Long, Instant]]" shouldNot typeCheck
    "implicitly[ValueClassLike[URI, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, URI]]" shouldNot typeCheck
  }

  test("Extract String to ZonedDateTime") {
    val testRoute = path("test" / Segment.to[ZonedDateTime]) { zdt =>
      complete(zdt.toString)
    }
    Get("/test/2011-12-03T10:15:30+01:00") ~> testRoute ~> check {
      responseAs[String] shouldEqual "2011-12-03T10:15:30+01:00"
    }
  }

  test("Extract Int to DayOfWeek") {
    val testRoute = path("test" / IntNumber.to[DayOfWeek]) { dayOfWeek =>
      complete(dayOfWeek.getValue.toString)
    }
    Get("/test/1") ~> testRoute ~> check {
      responseAs[String] shouldEqual "1"
    }
  }

  test("Extract Long to Instant ") {
    val testRoute = path("test" / LongNumber.to[Instant]) { instant =>
      complete(instant.toEpochMilli.toString)
    }
    Get("/test/1621258399") ~> testRoute ~> check {
      responseAs[String] shouldEqual "1621258399"
    }
  }
  test("Extract Double as tagged Double") {
    val testRoute = path("test" / DoubleNumber.as[TestDouble]) { test =>
      complete(test.toString)
    }
    Get("/test/1.23") ~> testRoute ~> check {
      responseAs[String] shouldEqual "1.23"
    }
  }

  test("Extract Long as tagged Long") {
    val testRoute = path("test" / LongNumber.as[Id]) { id =>
      complete(id.toString)
    }
    Get("/test/123456") ~> testRoute ~> check {
      responseAs[String] shouldEqual "123456"
    }
  }

  test("Extract UUID as tagged UUID") {
    val testRoute = path("test" / JavaUUID.as[TestId]) { id =>
      complete(id.toString)
    }
    Get("/test/ce7a7cf1-8c00-49a9-a963-9fd119dd0642") ~> testRoute ~> check {
      responseAs[String] shouldEqual "ce7a7cf1-8c00-49a9-a963-9fd119dd0642"
    }
  }

  test("Extract String as Enum") {
    val testRoute = path("test" / Segment.asEnum[Greeting]) { greeting =>
      complete(greeting.toString)
    }
    Get("/test/hello") ~> testRoute ~> check {
      responseAs[String] shouldEqual "Hello"
    }
  }

  test("Extract String as value class") {
    val testRoute = path("test" / Segment.as[S]) { item =>
      complete(item.toString)
    }
    Get("/test/check") ~> testRoute ~> check {
      responseAs[String] shouldEqual "S(check)"
    }
  }

  test("Extract Int as ValueEnum") {
    val testRoute = path("test" / IntNumber.asValueEnum[LibraryItem]) { item =>
      complete(item.toString)
    }
    Get("/test/1") ~> testRoute ~> check {
      responseAs[String] shouldEqual "Book"
    }
  }

  test("Extract String to URI as tagged URI") {
    val testRoute = path("test" / Segment.to[URI].as[TestTaggedUri]) { id =>
      complete(id.toString)
    }
    Get("/test/ce7a7cf1-8c00-49a9-a963-9fd119dd0642") ~> testRoute ~> check {
      responseAs[String] shouldEqual "ce7a7cf1-8c00-49a9-a963-9fd119dd0642"
    }
  }

  test("A value that is not one of the enum does not match") {
    val testRoute = path("test" / Segment.asEnum[Greeting])(greeting => complete(greeting.toString)) ~
      path("test" / Segment)(other => complete(s"other $other"))
    Get("/test/nope") ~> testRoute ~> check {
      responseAs[String] shouldEqual "other nope"
    }
    Get("/test/nope") ~> Route.seal(path("test" / Segment.asEnum[Greeting])(greeting => complete(greeting.toString))) ~> check {
      status shouldEqual Status.NotFound
    }
  }

  test("A value that is not one of the value enum does not match") {
    val testRoute = path("test" / IntNumber.asValueEnum[LibraryItem])(item => complete(item.toString)) ~
      path("test" / Segment)(other => complete(s"other $other"))
    Get("/test/9") ~> testRoute ~> check {
      responseAs[String] shouldEqual "other 9"
    }
  }

  test("A value that a value class rejects does not match") {
    val testRoute = path("test" / IntNumber.as[Http4sStirMatchersTests.Positive])(positive => complete(positive.toString)) ~
      path("test" / Segment)(other => complete(s"other $other"))
    Get("/test/1") ~> testRoute ~> check {
      responseAs[String] shouldEqual "Positive(1)"
    }
    Get("/test/0") ~> testRoute ~> check {
      responseAs[String] shouldEqual "other 0"
    }
  }

  test("A value that an InstanceConverter rejects does not match") {
    val testRoute = path("test" / IntNumber.to[DayOfWeek])(day => complete(day.toString)) ~
      path("test" / Segment)(other => complete(s"other $other"))
    Get("/test/1") ~> testRoute ~> check {
      responseAs[String] shouldEqual "MONDAY"
    }
    Get("/test/8") ~> testRoute ~> check {
      responseAs[String] shouldEqual "other 8"
    }
  }
}
