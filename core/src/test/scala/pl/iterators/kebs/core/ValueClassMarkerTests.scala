package pl.iterators.kebs.core

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.macros.{CaseClass1ToValueClass, ValueClass, ValueClassLike}

object ValueClassMarkerTestDomain {
  final case class Marked(value: String)
  object Marked { implicit val valueClass: ValueClass[Marked] = ValueClass.derived }
  final case class MarkedPositive(value: Int)
  object MarkedPositive {
    implicit val valueClass: ValueClass[MarkedPositive] = ValueClass.derived
    def apply(value: Int): MarkedPositive               = {
      require(value > 0, "not positive")
      new MarkedPositive(value)
    }
  }
  final case class Unmarked(value: String)
  final case class Own(value: String)
  object Own {
    implicit val valueClassLike: ValueClassLike[Own, String] = new ValueClassLike[Own, String](Own(_), _.value, _ => Left("own"))
  }
}

class ValueClassMarkerTests extends AnyFunSuite with Matchers {
  import ValueClassMarkerTestDomain._

  test("a marked type has a ValueClassLike without CaseClass1ToValueClass") {
    val vcl = implicitly[ValueClassLike[Marked, String]]
    vcl.apply("a") shouldBe Marked("a")
    vcl.unapply(Marked("b")) shouldBe "b"
    implicitly[ValueClassLike[MarkedPositive, Int]].from(0) shouldBe Left("requirement failed: not positive")
  }

  test("an unmarked type has none") {
    "implicitly[ValueClassLike[Unmarked, String]]" shouldNot typeCheck
  }

  test("a hand-written instance wins over CaseClass1ToValueClass") {
    import CaseClass1ToValueClass._
    implicitly[ValueClassLike[Own, String]].from("x") shouldBe Left("own")
    implicitly[ValueClassLike[Unmarked, String]].apply("u") shouldBe Unmarked("u")
  }

  test("only a type with exactly one field can be marked") {
    "final case class Two(a: Int, b: Int); implicit val two: ValueClass[Two] = ValueClass.derived[Two]" shouldNot typeCheck
  }
}
