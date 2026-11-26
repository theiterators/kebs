package pl.iterators.kebs.http4s

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.macros.ValueClassLike

object Http4sValidationTestDomain {
  final case class Code(value: String)
  object Code {
    implicit val valueClassLike: ValueClassLike[Code, String] =
      new ValueClassLike[Code, String](Code(_), _.value, s => if (s.length == 3) Right(Code(s)) else Left("not three letters"))
  }
}

class Http4sValidationTests extends AnyFunSuite with Matchers {
  import Http4sValidationTestDomain._

  implicit private val runtime: IORuntime = IORuntime.global

  private object CodeParam extends QueryParamDecoderMatcher[Code]("code")
  private val CodeVar = WrappedString[Code]

  private val routes = HttpRoutes.of[IO] {
    case GET -> Root / "path" / CodeVar(code)     => Ok(code.value)
    case GET -> Root / "path" / other             => Ok(s"other $other")
    case GET -> Root / "query" :? CodeParam(code) => Ok(code.value)
  }

  private def run(uri: Uri): (Status, String) = {
    val response = routes.orNotFound.run(Request[IO](Method.GET, uri)).unsafeRunSync()
    (response.status, response.as[String].unsafeRunSync())
  }

  test("a value class's own from decides in paths") {
    run(uri"/path/abc") shouldBe ((Status.Ok, "abc"))
    run(uri"/path/abcd") shouldBe ((Status.Ok, "other abcd"))
  }

  test("a value class's own from decides in queries") {
    run(uri"/query?code=abc") shouldBe ((Status.Ok, "abc"))
    QueryParamDecoder[Code].decode(QueryParameterValue("abcd")).toEither.left.map(_.head.sanitized) shouldBe Left("not three letters")
  }
}
