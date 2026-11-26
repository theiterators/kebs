package pl.iterators.kebs.http4sstir

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.enums.EnumNaming
import pl.iterators.kebs.enumeratum.KebsEnumeratum
import pl.iterators.kebs.http4sstir.matchers.KebsHttp4sStirMatchers
import pl.iterators.kebs.http4sstir.unmarshallers.KebsHttp4sStirUnmarshallers
import pl.iterators.kebs.testkit.model.Color
import pl.iterators.stir.server.Directives
import pl.iterators.stir.testkit.{RouteTestTimeout, ScalatestRouteTest}

import scala.concurrent.duration._

class Http4sStirEnumNamingTests
    extends AnyFunSuite
    with Matchers
    with Directives
    with ScalatestRouteTest
    with KebsHttp4sStirUnmarshallers
    with KebsHttp4sStirMatchers
    with KebsEnumeratum {
  implicit val routeTestTimeout: RouteTestTimeout    = RouteTestTimeout(5.seconds)
  implicit def runtime: cats.effect.unsafe.IORuntime = cats.effect.unsafe.IORuntime.global
  implicit val enumNaming: EnumNaming                = EnumNaming.screamingSnakeCase

  test("enum parameters use the EnumNaming in scope") {
    val route = parameters(Symbol("color").as[Color])(color => complete(color.toString))
    Get("/?color=dark_blue") ~> route ~> check(responseAs[String] shouldEqual "DarkBlue")
  }

  test("enum path segments use the EnumNaming in scope") {
    val route = path("test" / Segment.asEnum[Color])(color => complete(color.toString))
    Get("/test/DARK_BLUE") ~> route ~> check(responseAs[String] shouldEqual "DarkBlue")
  }
}
