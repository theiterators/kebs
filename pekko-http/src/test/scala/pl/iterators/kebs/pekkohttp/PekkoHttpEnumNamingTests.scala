package pl.iterators.kebs.pekkohttp

import org.apache.pekko.http.scaladsl.server.Directives
import org.apache.pekko.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.enums.EnumNaming
import pl.iterators.kebs.enumeratum.KebsEnumeratum
import pl.iterators.kebs.pekkohttp.matchers._
import pl.iterators.kebs.pekkohttp.unmarshallers._
import pl.iterators.kebs.testkit.model.Color

class PekkoHttpEnumNamingTests extends AnyFunSuite with Matchers with Directives with ScalatestRouteTest with KebsEnumeratum {
  implicit val enumNaming: EnumNaming = EnumNaming.screamingSnakeCase

  test("enum parameters use the EnumNaming in scope") {
    val route = parameters(Symbol("color").as[Color])(color => complete(color.toString))
    Get("/?color=dark_blue") ~> route ~> check(responseAs[String] shouldEqual "DarkBlue")
  }

  test("enum path segments use the EnumNaming in scope") {
    val route = path("test" / Segment.asEnum[Color])(color => complete(color.toString))
    Get("/test/DARK_BLUE") ~> route ~> check(responseAs[String] shouldEqual "DarkBlue")
  }
}
