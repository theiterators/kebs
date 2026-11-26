package pl.iterators.kebs.core

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.macros.{CaseClass1ToValueClass, ValueClassLike}

object ValueClassLikeTestDomain {
  final case class Plain(value: String)
  final case class Box[A](a: A)
  final case class Positive(value: Int)
  object Positive {
    def apply(value: Int): Positive = {
      require(value > 0, "must be positive")
      new Positive(value)
    }
  }
  final case class Email(value: String)
  object Email {
    def apply(value: String): Email = new Email(value.toLowerCase)
  }
  final case class Two(a: Int, b: Int)
  final case class Wrapped(value: String) extends AnyVal
  final case class Validated(value: Int)  extends AnyVal
  object Validated {
    def apply(value: Int): Validated = {
      require(value > 0, "must be positive")
      new Validated(value)
    }
  }
  final class NotCase(val value: Int)
}

class ValueClassLikeTests extends AnyFunSuite with Matchers with CaseClass1ToValueClass {
  import ValueClassLikeTestDomain._

  test("derives apply/unapply for a single-field case class") {
    val rep = implicitly[ValueClassLike[Plain, String]]
    rep.apply("a") shouldBe Plain("a")
    rep.unapply(Plain("b")) shouldBe "b"
  }

  test("infers the field type through an outer implicit") {
    def find[T, A](implicit rep: ValueClassLike[T, A]): ValueClassLike[T, A] = rep
    find[Plain, String].unapply(Plain("c")) shouldBe "c"
  }

  test("generic case class") {
    implicitly[ValueClassLike[Box[Int], Int]].apply(1) shouldBe Box(1)
  }

  test("uses the companion apply (validation)") {
    val rep = implicitly[ValueClassLike[Positive, Int]]
    an[IllegalArgumentException] should be thrownBy rep.apply(-1)
  }

  test("uses the companion apply (normalisation)") {
    implicitly[ValueClassLike[Email, String]].apply("A@B.PL").value shouldBe "a@b.pl"
  }

  test("no instance for a 2-field case class or a non-case class") {
    assertDoesNotCompile("implicitly[ValueClassLike[Two, Int]]")
    assertDoesNotCompile("implicitly[ValueClassLike[NotCase, Int]]")
  }

  test("AnyVal case class (Scala 3 synthesises no Mirror for it)") {
    val rep = implicitly[ValueClassLike[Wrapped, String]]
    rep.apply("a") shouldBe Wrapped("a")
    rep.unapply(Wrapped("b")) shouldBe "b"
  }

  test("AnyVal case class: field type inferred, companion apply honoured") {
    def find[T, A](implicit rep: ValueClassLike[T, A]): ValueClassLike[T, A] = rep
    find[Wrapped, String].unapply(Wrapped("c")) shouldBe "c"
    an[IllegalArgumentException] should be thrownBy find[Validated, Int].apply(0)
  }
}
