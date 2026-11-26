package pl.iterators.kebs.core

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.enums.EnumLike

object EnumLikeTestDomain {
  sealed trait Tone
  case object Light   extends Tone
  case object Dark    extends Tone
  case object DarkRed extends Tone
  case object A1      extends Tone
  case object A2      extends Tone
  case object Clash   extends Tone

  val names: Map[Tone, String] = Map(Light -> "Light", Dark -> "Dark", DarkRed -> "DarkRed", A1 -> "a1", A2 -> "A2", Clash -> "DARK")
  val tones: List[Tone]        = List(Light, Dark, DarkRed, A1, A2, Clash)
}

class EnumLikeTests extends AnyFunSuite with Matchers {
  import EnumLikeTestDomain._

  private val enumLike = EnumLike[Tone](tones, names)

  test("case-insensitive lookup") {
    enumLike.withNameInsensitiveOption("darkred") shouldBe Some(DarkRed)
    enumLike.withNameInsensitiveOption("nope") shouldBe None
  }

  test("case-insensitive collision: first in declaration order wins") {
    enumLike.withNameInsensitiveOption("dark") shouldBe Some(Dark)
  }

  test("case-insensitive lookup does not depend on the locale (equalsIgnoreCase semantics)") {
    EnumLike[String](List("İx"), identity).withNameInsensitiveOption("ix") shouldBe Some("İx")
  }

  test("uppercase-only lookup") {
    enumLike.withNameUppercaseOnlyOption("DARKRED") shouldBe Some(DarkRed)
    enumLike.withNameUppercaseOnlyOption("DarkRed") shouldBe None
    enumLike.withNameUppercaseOnlyOption("DARK") shouldBe Some(Dark)
  }

  test("lowercase-only lookup") {
    enumLike.withNameLowercaseOnlyOption("a2") shouldBe Some(A2)
    enumLike.withNameLowercaseOnlyOption("A2") shouldBe None
  }

  test("derived lookups delegate to the maps") {
    enumLike.withNameIgnoreCase("LIGHT") shouldBe Light
    enumLike.valueOfIgnoreCase("dArK") shouldBe Dark
    an[NoSuchElementException] should be thrownBy enumLike.withNameUppercaseOnly("Light")
  }
}
