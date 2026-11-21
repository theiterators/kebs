package pl.iterators.kebs.enums

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

object EnumSelectionTestDomain {
  sealed trait Plain
  case object P1 extends Plain
  case object P2 extends Plain
  object Planet  extends Enumeration {
    val Mercury, Venus = Value
  }
}

class EnumSelectionTests extends AnyFunSuite with Matchers with KebsEnum {
  import EnumSelectionTestDomain._

  test("plain sealed trait of case objects gets no EnumLike from KebsEnum") {
    implicitly[PickEnumLike[Plain]].chosen shouldBe "fallback"
  }

  test("abstract Enumeration#Value falls back silently") {
    def pick[E <: Enumeration#Value]: PickEnumLike[E] = implicitly[PickEnumLike[E]]
    pick[Planet.Value].chosen shouldBe "fallback"
    implicitly[PickEnumLike[Enumeration#Value]].chosen shouldBe "fallback"
  }

  test("Enumeration values get an EnumLike") {
    implicitly[PickEnumLike[Planet.Value]].chosen shouldBe "enum"
  }
}
