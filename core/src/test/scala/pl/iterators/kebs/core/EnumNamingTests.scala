package pl.iterators.kebs.core

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.enums.{EnumLike, EnumNaming}
import pl.iterators.kebs.core.naming.Naming

object EnumNamingTestDomain {
  sealed trait Tone
  case object Light   extends Tone
  case object DarkRed extends Tone
  val tones: EnumLike[Tone] = EnumLike[Tone](List(Light, DarkRed), _.toString)
}

class EnumNamingTests extends AnyFunSuite with Matchers {
  import EnumNamingTestDomain._

  test("identity keeps the EnumLike as it is") {
    EnumNaming.default.applyTo(tones) should be theSameInstanceAs tones
  }

  test("a naming style renames the entries") {
    EnumNaming.screamingSnakeCase.applyTo(tones).names shouldBe List("LIGHT", "DARK_RED")
    EnumNaming.uppercase.applyTo(tones).getName(DarkRed) shouldBe "DARKRED"
  }

  test("lookup is case-insensitive by default, strict on request") {
    val named = EnumNaming.screamingSnakeCase.applyTo(tones)
    EnumNaming.screamingSnakeCase.lookup(named, "dark_red") shouldBe Some(DarkRed)
    EnumNaming(Naming.screamingSnakeCase, caseInsensitive = false).lookup(named, "dark_red") shouldBe None
    EnumNaming(Naming.screamingSnakeCase, caseInsensitive = false).lookup(named, "DARK_RED") shouldBe Some(DarkRed)
  }

  test("an implicit EnumNaming in scope beats the default") {
    implicitly[EnumNaming] shouldBe EnumNaming.default
    locally {
      implicit val enumNaming: EnumNaming = EnumNaming.kebabCase
      implicitly[EnumNaming] shouldBe EnumNaming.kebabCase
    }
  }
}
