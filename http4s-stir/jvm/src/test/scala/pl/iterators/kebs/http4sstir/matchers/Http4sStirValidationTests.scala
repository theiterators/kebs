package pl.iterators.kebs.http4sstir.matchers

import pl.iterators.kebs.http4sstir.unmarshallers.KebsHttp4sStirUnmarshallers
import pl.iterators.stir.server.{Directives, MalformedQueryParamRejection}
import pl.iterators.stir.testkit.{RouteTestTimeout, ScalatestRouteTest}

import scala.concurrent.duration._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.macros.ValueClassLike

object Http4sStirValidationTestsDomain {
  final case class Code(value: String)
  object Code {
    implicit val valueClassLike: ValueClassLike[Code, String] =
      new ValueClassLike[Code, String](Code(_), _.value, s => if (s.length == 3) Right(Code(s)) else Left("not three letters"))
  }
}

// without CaseClass1ToValueClass, which would win over Code's own instance
class Http4sStirValidationTests
    extends AnyFunSuite
    with Matchers
    with Directives
    with ScalatestRouteTest
    with ScalaFutures
    with KebsHttp4sStirUnmarshallers {
  import Http4sStirValidationTestsDomain._
  implicit val routeTestTimeout: RouteTestTimeout    = RouteTestTimeout(5.seconds)
  implicit def runtime: cats.effect.unsafe.IORuntime = cats.effect.unsafe.IORuntime.global

  test("a value class's own from decides in paths") {
    val testRoute = path("test" / Segment.as[Code])(code => complete(code.value)) ~
      path("test" / Segment)(other => complete(s"other $other"))
    Get("/test/abc") ~> testRoute ~> check {
      responseAs[String] shouldEqual "abc"
    }
    Get("/test/abcd") ~> testRoute ~> check {
      responseAs[String] shouldEqual "other abcd"
    }
  }

  test("a value class's own from decides in parameters") {
    val testRoute = parameters(Symbol("code").as[Code])(code => complete(code.value))
    Get("/?code=abc") ~> testRoute ~> check {
      responseAs[String] shouldEqual "abc"
    }
    Get("/?code=abcd") ~> testRoute ~> check {
      rejection shouldEqual MalformedQueryParamRejection("code", "not three letters", None)
    }
  }
}
