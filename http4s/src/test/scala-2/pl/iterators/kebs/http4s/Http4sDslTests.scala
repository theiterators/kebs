package pl.iterators.kebs.http4s

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.instances.KebsInstances._
import pl.iterators.kebs.enumeratum.KebsEnumeratum

import java.time.Year
import java.util.Currency
import pl.iterators.kebs.instances.KebsInstances._
import pl.iterators.kebs.http4s._
import pl.iterators.kebs.enumeratum.KebsEnumeratum

class Http4sDslTests extends AnyFunSuite with Matchers with KebsEnumeratum {
  import pl.iterators.kebs.http4s.domain.Domain._

  implicit val runtime: IORuntime = cats.effect.unsafe.IORuntime.global

  object AgeQueryParamDecoderMatcher extends QueryParamDecoderMatcher[Age]("age")

  object OptionalYearParamDecoderMatcher extends OptionalQueryParamDecoderMatcher[Year]("year")

  object ValidatingColorQueryParamDecoderMatcher extends ValidatingQueryParamDecoderMatcher[Color]("color")

  // this indirection comes from: https://github.com/scala/bug/issues/884
  val AgeVar = WrappedInt[Age]
  val CurrencyVar = InstanceString[Currency]
  val UserIdVar = WrappedUUID[UserId]
  val ColorVar = EnumString[Color]

  val routes = HttpRoutes.of[IO] {
    case GET -> Root / "WrappedInt" / AgeVar(age) => Ok(age.toString)
    case GET -> Root / "InstanceString" / CurrencyVar(currency) => Ok(currency.getClass.toString)
    case GET -> Root / "EnumString" / ColorVar(color) => Ok(color.toString)
    case GET -> Root / "WrappedUUID" / UserIdVar(userId) => Ok(userId.toString)
    case GET -> Root / "WrappedIntParam" :? AgeQueryParamDecoderMatcher(age) => Ok(age.toString)
    case GET -> Root / "InstanceIntParam" :? OptionalYearParamDecoderMatcher(year) => Ok(year.toString)
    case GET -> Root / "EnumStringParam" :? ValidatingColorQueryParamDecoderMatcher(color) => Ok(color.toString)
  }

  private def runPathGetBody(path: Uri): String = {
    routes.orNotFound.run(Request(method = Method.GET, uri = path)).unsafeRunSync().body.compile.fold[String]("")(_ + _.toChar).unsafeRunSync()
  }

  test("WrappedInt + Opaque") {
    runPathGetBody(uri"/WrappedInt/42") shouldBe "42"
    runPathGetBody(uri"/WrappedInt/-42") shouldBe "Not found"
  }

  test("InstanceString + Currency") {
    runPathGetBody(uri"/InstanceString/USD") shouldBe "class java.util.Currency"
    runPathGetBody(uri"/InstanceString/NOPE") shouldBe "Not found"
  }

  test("WrappedUUID") {
    runPathGetBody(uri"/WrappedUUID/8cc82b40-71bc-4e50-ac7e-8227013f37ea") shouldBe "UserId(8cc82b40-71bc-4e50-ac7e-8227013f37ea)"
    runPathGetBody(uri"/WrappedUUID/NOPE") shouldBe "Not found"
  }

  test("EnumString") {
    runPathGetBody(uri"/EnumString/Red") shouldBe "Red"
    runPathGetBody(uri"/EnumString/BlUe") shouldBe "Blue"
    runPathGetBody(uri"/EnumString/GrEEn") shouldBe "Green"
    runPathGetBody(uri"/EnumString/Yellow") shouldBe "Not found"
  }

  test("WrappedIntParam + Opaque") {
    runPathGetBody(uri"/WrappedIntParam?age=42") shouldBe "42"
    runPathGetBody(uri"/WrappedIntParam?age=-42") shouldBe "Not found"
  }

  test("InstanceIntParam + Year") {
    runPathGetBody(uri"/InstanceIntParam?year=2022") shouldBe "Some(2022)"
    runPathGetBody(uri"/InstanceIntParam?year=-2147483647") shouldBe "Not found"
  }

  test("EnumStringParam") {
    runPathGetBody(uri"/EnumStringParam?color=RED") shouldBe "Valid(Red)"
    runPathGetBody(uri"/EnumStringParam?color=YELLow") shouldBe "Invalid(NonEmptyList(org.http4s.ParseFailure: enum case not found: YELLow: enum case not found: YELLow))"
  }
}
