import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.testkit.ScalatestRouteTest
import enumeratum.{Enum, EnumEntry}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.instances.TimeInstances.{DayOfWeekNumber, ZonedDateTimeString}
import pl.iterators.kebs.matchers._
import pl.iterators.kebs.tag.meta.tagged
import pl.iterators.kebs.tagged._

import java.time.{DayOfWeek, ZonedDateTime}

@tagged trait Tags {
  trait IdTag
  type Id = Long @@ IdTag
}
object Domain extends Tags

class AkkaHttpMatchersTests
    extends AnyFunSuite
    with Matchers
    with Directives
    with ScalatestRouteTest
    with ScalaFutures
    with ZonedDateTimeString
    with DayOfWeekNumber {

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
  test("Extract tagged instance") {
    val testRoute = path("test" / Segment.asLong[Id]) { id =>
      complete(id.toString)
    }
    Get("/test/123456") ~> testRoute ~> check {
      responseAs[String] shouldEqual "123456"
    }
  }
}
