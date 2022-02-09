package pl.iterators.kebs.matchers

import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.Domain._
import pl.iterators.kebs.instances.net.URIString
import pl.iterators.kebs.instances.time.mixins.InstantEpochMilliLong
import pl.iterators.kebs.instances.time.{DayOfWeekInt, ZonedDateTimeString}

import java.net.URI
import java.time.{DayOfWeek, Instant, ZonedDateTime}

class AkkaHttpMatchersTests
    extends AnyFunSuite
    with Matchers
    with Directives
    with ScalatestRouteTest
    with ScalaFutures
    with ZonedDateTimeString
    with DayOfWeekInt
    with InstantEpochMilliLong
    with URIString {

  test("No CaseClass1Rep implicits derived") {
    import pl.iterators.kebs.macros.CaseClass1Rep

    "implicitly[CaseClass1Rep[DayOfWeek, Int]]" shouldNot typeCheck
    "implicitly[CaseClass1Rep[Int, DayOfWeek]]" shouldNot typeCheck
    "implicitly[CaseClass1Rep[Instant, Long]]" shouldNot typeCheck
    "implicitly[CaseClass1Rep[Long, Instant]]" shouldNot typeCheck
    "implicitly[CaseClass1Rep[URI, String]]" shouldNot typeCheck
    "implicitly[CaseClass1Rep[String, URI]]" shouldNot typeCheck
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
    val testRoute = path("test" / EnumSegment.as[Greeting]) { greeting =>
      complete(greeting.toString)
    }
    Get("/test/hello") ~> testRoute ~> check {
      responseAs[String] shouldEqual "Hello"
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
}
