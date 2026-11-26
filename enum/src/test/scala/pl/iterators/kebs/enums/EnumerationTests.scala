package pl.iterators.kebs.enums

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.enums.EnumLike

object EnumerationTestDomain {
  object Planet extends Enumeration {
    type Planet = Value
    val Mercury, Venus, Earth = Value
  }
}

class EnumerationTests extends AnyFunSuite with Matchers with KebsEnum {
  import EnumerationTestDomain._

  test("EnumLike for scala.Enumeration values") {
    val planets = implicitly[EnumLike[Planet.Value]]
    planets.values shouldBe Planet.values.toList
    planets.getName(Planet.Venus) shouldBe "Venus"
    planets.withNameInsensitiveOption("earth") shouldBe Some(Planet.Earth)
  }

  test("EnumLike through a type alias of Enumeration#Value") {
    implicitly[EnumLike[Planet.Planet]].values shouldBe List(Planet.Mercury, Planet.Venus, Planet.Earth)
  }
}
