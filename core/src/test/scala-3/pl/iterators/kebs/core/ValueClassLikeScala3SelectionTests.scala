package pl.iterators.kebs.core

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.macros.{CaseClass1ToValueClass, ValueClassLike}

object ValueClassLikeScala3SelectionDomain {
  final case class ProtectedCtor protected (value: Int)
  object ProtectedCtor {
    def from(value: Int): ProtectedCtor = new ProtectedCtor(value)
  }
  final case class PrivateField(private val value: Int)
}

/** Scala 3 synthesises a Mirror for these, so kebs 2 gave them a ValueClassLike (via Mirror.fromProduct / productElement); kebs 3 must
  * keep doing so instead of failing after the Mirror guard selected the implicit.
  */
class ValueClassLikeScala3SelectionTests extends AnyFunSuite with Matchers with CaseClass1ToValueClass {
  import ValueClassLikeScala3SelectionDomain._

  test("protected-constructor value class gets the flat instance and round-trips") {
    implicitly[Pick[ProtectedCtor]].chosen shouldBe "flat"
    val rep = implicitly[ValueClassLike[ProtectedCtor, Int]]
    rep.unapply(rep.apply(5)) shouldBe 5
  }

  test("case class with a private field gets the flat instance and round-trips") {
    implicitly[Pick[PrivateField]].chosen shouldBe "flat"
    val rep = implicitly[ValueClassLike[PrivateField, Int]]
    rep.unapply(rep.apply(7)) shouldBe 7
  }
}
