import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.testkit.ScalatestRouteTest
import enumeratum.{Enum, EnumEntry}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.instances.TimeInstances.{DayOfWeekInt, ZonedDateTimeString}
import pl.iterators.kebs.matchers._
import pl.iterators.kebs.tag.meta.tagged
import pl.iterators.kebs.tagged._

import java.time.{DayOfWeek, ZonedDateTime}
import java.util.UUID

@tagged trait Tags {
  trait IdTag
  type Id = Long @@ IdTag

  trait TestIdTag
  type TestId = UUIDId @@ TestIdTag

  type UUIDId = UUID
  object UUIDId {
    def generate[T]: UUIDId @@ T = UUID.randomUUID().taggedWith[T]
    def fromString[T](str: String): UUIDId @@ T =
      UUID.fromString(str).taggedWith[T]
  }
}

object Domain extends Tags

class AkkaHttpMatchersTests
    extends AnyFunSuite
    with Matchers
    with Directives
    with ScalatestRouteTest
    with ScalaFutures
    with ZonedDateTimeString
    with DayOfWeekInt {

  test("Extract String instance") {
    val testRoute = path("test" / Segment.asString[ZonedDateTime]) { zdt =>
      complete(zdt.toString)
    }
    Get("/test/2011-12-03T10:15:30+01:00") ~> testRoute ~> check {
      responseAs[String] shouldEqual "2011-12-03T10:15:30+01:00"
    }
  }

  test("Extract Int instance ") {
    val testRoute = path("test" / Segment.asInt[DayOfWeek]) { dayOfWeek =>
      complete(dayOfWeek.getValue.toString)
    }
    Get("/test/1") ~> testRoute ~> check {
      responseAs[String] shouldEqual "1"
    }
  }

  sealed trait Greeting extends EnumEntry
  object Greeting extends Enum[Greeting] {
    case object Hello   extends Greeting
    case object GoodBye extends Greeting
    case object Hi      extends Greeting
    case object Bye     extends Greeting

    val values = findValues
  }

  test("Extract Enum instance") {
    val testRoute = path("test" / Segment.asEnum[Greeting]) { greeting =>
      complete(greeting.toString)
    }
    Get("/test/hello") ~> testRoute ~> check {
      responseAs[String] shouldEqual "Hello"
    }
  }

  import Domain._
  test("Extract tagged primitive instance") {
    val testRoute = path("test" / Segment.asLong[Id]) { id =>
      complete(id.toString)
    }
    Get("/test/123456") ~> testRoute ~> check {
      responseAs[String] shouldEqual "123456"
    }
  }

  test("Extract tagged UUID instance") {
    val testRoute = path("test" / Segment.asUUID[TestId]) { id =>
      complete(id.toString)
    }
    Get("/test/ce7a7cf1-8c00-49a9-a963-9fd119dd0642") ~> testRoute ~> check {
      responseAs[String] shouldEqual "ce7a7cf1-8c00-49a9-a963-9fd119dd0642"
    }
  }
}
