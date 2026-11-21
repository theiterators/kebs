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

  sealed trait DataUnit
  case object Megabit  extends DataUnit
  case object Megabyte extends DataUnit
  val units: EnumLike[DataUnit] = EnumLike[DataUnit](List(Megabit, Megabyte), { case Megabit => "Mb"; case Megabyte => "MB" })

  class Scoped {
    implicit val enumNaming: EnumNaming = EnumNaming.snakeCase
    def outer: EnumNaming               = implicitly[EnumNaming]
    def inner: EnumNaming               = {
      implicit val enumNaming: EnumNaming = EnumNaming.kebabCase
      implicitly[EnumNaming]
    }
  }
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

  test("an exact match wins over a case-insensitive one") {
    EnumNaming.default.lookup(units, "MB") shouldBe Some(Megabyte)
    EnumNaming.default.lookup(units, "Mb") shouldBe Some(Megabit)
    EnumNaming.default.lookup(units, "mB") shouldBe Some(Megabit)
  }

  test("importing EnumNaming's members does not make the default ambiguous") {
    import EnumNaming._
    implicit val enumNaming: EnumNaming = kebabCase
    implicitly[EnumNaming] shouldBe kebabCase
  }

  test("an EnumNaming is overridden by shadowing the same name") {
    val scoped = new Scoped
    scoped.outer shouldBe EnumNaming.snakeCase
    scoped.inner shouldBe EnumNaming.kebabCase
  }

  test("an implicit EnumNaming in scope beats the default") {
    implicitly[EnumNaming] shouldBe EnumNaming.default
    locally {
      implicit val enumNaming: EnumNaming = EnumNaming.kebabCase
      implicitly[EnumNaming] shouldBe EnumNaming.kebabCase
    }
  }
}
