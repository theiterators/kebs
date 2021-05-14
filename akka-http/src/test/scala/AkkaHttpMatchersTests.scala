import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.testkit.ScalatestRouteTest
import enumeratum.{Enum, EnumEntry}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.instances.TimeInstances.{DayOfWeekNumber, ZonedDateTimeString}
import pl.iterators.kebs.matchers._
import pl.iterators.kebs.tagged._

import java.time.{DayOfWeek, ZonedDateTime}

class AkkaHttpMatchersTests
    extends AnyFunSuite
    with Matchers
    with Directives
    with ScalatestRouteTest
    with ScalaFutures
    with ZonedDateTimeString
    with DayOfWeekNumber {

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

  sealed trait Greeting extends EnumEntry
  object Greeting extends Enum[Greeting] {
    case object Hello   extends Greeting
    case object GoodBye extends Greeting
    case object Hi      extends Greeting
    case object Bye     extends Greeting

    val values = findValues
  }

  test("Extract Enum instance") {
    val testRoute = path("test" / Segment.asEnum[Greeting](Greeting)) { greeting =>
      complete(greeting.toString)
    }
    Get("/test/hello") ~> testRoute ~> check {
      responseAs[String] shouldEqual "Hello"
    }
  }

  trait IdTag
  type Id = Long @@ IdTag

  test("Extract tagged instance") {
    val testRoute = path("test" / LongNumber.asTagged[Id]) { id =>
      complete(id.toString)
    }
    Get("/test/123456") ~> testRoute ~> check {
      responseAs[String] shouldEqual "123456"
    }
  }
}
