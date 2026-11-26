package pl.iterators.kebs.pekkohttp.matchers

import org.apache.pekko.http.scaladsl.server.{Directives, MalformedQueryParamRejection}
import org.apache.pekko.http.scaladsl.testkit.ScalatestRouteTest
import pl.iterators.kebs.pekkohttp.unmarshallers.KebsPekkoHttpUnmarshallers
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.macros.{CaseClass1ToValueClass, ValueClassLike}

object PekkoHttpValidationTestsDomain {
  final case class Code(value: String)
  object Code {
    implicit val valueClassLike: ValueClassLike[Code, String] =
      new ValueClassLike[Code, String](Code(_), _.value, s => if (s.length == 3) Right(Code(s)) else Left("not three letters"))
  }
}

// Code's own instance wins over CaseClass1ToValueClass
class PekkoHttpValidationTests
    extends AnyFunSuite
    with Matchers
    with Directives
    with ScalatestRouteTest
    with ScalaFutures
    with KebsPekkoHttpUnmarshallers
    with CaseClass1ToValueClass {
  import PekkoHttpValidationTestsDomain._

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
