package pl.iterators.kebs.core

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.macros.{CaseClass1ToValueClass, ValueClassLike}

/** A type class with a flat instance (needs ValueClassLike) and a low-priority fallback: a type without a ValueClassLike must get the
  * fallback, never a compile error - this is how every flat format in kebs is searched.
  */
final class Pick[A](val chosen: String)
trait LowPriorityPick {
  implicit def fallback[A]: Pick[A] = new Pick[A]("fallback")
}
object Pick extends LowPriorityPick {
  implicit def flat[T, A](implicit rep: ValueClassLike[T, A]): Pick[T] = new Pick[T]("flat")
}

object ValueClassLikeSelectionDomain {
  final case class Plain(value: String)
  final case class Two(a: Int, b: Int)
  final class NotCase(val value: Int)
  final case class PrivateCtor private (value: String)
  object PrivateCtor {
    def from(value: String): PrivateCtor = new PrivateCtor(value)
  }
  final case class MultiList(a: Int)(val b: Int)
}

class ValueClassLikeSelectionTests extends AnyFunSuite with Matchers with CaseClass1ToValueClass {
  import ValueClassLikeSelectionDomain._

  test("single-field case class gets the flat instance") {
    implicitly[Pick[Plain]].chosen shouldBe "flat"
  }

  test("2-field case class falls back silently") {
    implicitly[Pick[Two]].chosen shouldBe "fallback"
  }

  test("non-case class falls back silently") {
    implicitly[Pick[NotCase]].chosen shouldBe "fallback"
  }

  test("private-constructor value class gets the flat instance and round-trips") {
    implicitly[Pick[PrivateCtor]].chosen shouldBe "flat"
    val rep = implicitly[ValueClassLike[PrivateCtor, String]]
    rep.unapply(rep.apply("x")) shouldBe "x"
  }

  test("case class with several parameter lists never fails compilation") {
    implicitly[Pick[MultiList]].chosen should (equal("flat") or equal("fallback"))
  }
}
