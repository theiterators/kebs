package pl.iterators.kebs.matchers

import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.Domain._
import pl.iterators.kebs.instances.TimeInstances.{DayOfWeekInt, InstantEpochMilliLong, ZonedDateTimeString}

import java.time.{DayOfWeek, Instant, ZonedDateTime}

class AkkaHttpMatchersTests
    extends AnyFunSuite
    with Matchers
    with Directives
    with ScalatestRouteTest
    with ScalaFutures
    with ZonedDateTimeString
    with DayOfWeekInt
    with InstantEpochMilliLong {

  test("Extract String instance") {
    val testRoute = path("test" / Segment.as[ZonedDateTime]) { zdt =>
      complete(zdt.toString)
    }
    Get("/test/2011-12-03T10:15:30+01:00") ~> testRoute ~> check {
      responseAs[String] shouldEqual "2011-12-03T10:15:30+01:00"
    }
  }

  test("Extract Int instance ") {
    val testRoute = path("test" / IntNumber.as[DayOfWeek]) { dayOfWeek =>
      complete(dayOfWeek.getValue.toString)
    }
    Get("/test/1") ~> testRoute ~> check {
      responseAs[String] shouldEqual "1"
    }
  }

  test("Extract Long instance ") {
    val testRoute = path("test" / LongNumber.as[Instant]) { instant =>
      complete(instant.toEpochMilli.toString)
    }
    Get("/test/1621258399") ~> testRoute ~> check {
      responseAs[String] shouldEqual "1621258399"
    }
  }
  test("Extract Double instance ") {
    val testRoute = path("test" / DoubleNumber.as[TestDouble]) { test =>
      complete(test.toString)
    }
    Get("/test/1.23") ~> testRoute ~> check {
      responseAs[String] shouldEqual "1.23"
    }
  }

  test("Extract tagged primitive instance") {
    val testRoute = path("test" / LongNumber.as[Id]) { id =>
      complete(id.toString)
    }
    Get("/test/123456") ~> testRoute ~> check {
      responseAs[String] shouldEqual "123456"
    }
  }

  test("Extract tagged UUID instance") {
    val testRoute = path("test" / JavaUUID.as[TestId]) { id =>
      complete(id.toString)
    }
    Get("/test/ce7a7cf1-8c00-49a9-a963-9fd119dd0642") ~> testRoute ~> check {
      responseAs[String] shouldEqual "ce7a7cf1-8c00-49a9-a963-9fd119dd0642"
    }
  }

  test("Extract Enum instance") {
    val testRoute = path("test" / EnumSegment.as[Greeting]) { greeting =>
      complete(greeting.toString)
    }
    Get("/test/hello") ~> testRoute ~> check {
      responseAs[String] shouldEqual "Hello"
    }
  }
}
