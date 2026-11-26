package pl.iterators.kebs.scalacheck

import org.scalacheck.Arbitrary
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.macros.{CaseClass1ToValueClass, ValueClassLike}

object ValidationGeneratorsTests {
  final case class Positive(value: Int) { require(value > 0, "not positive") }
  final case class Code(value: String)
  object Code {
    implicit val valueClassLike: ValueClassLike[Code, String] =
      new ValueClassLike[Code, String](Code(_), _.value, s => if (s.length == 3) Right(Code(s)) else Left("not three letters"))
  }
}

class ValidationGeneratorsTests extends AnyFunSuite with Matchers {
  import KebsArbitrarySupport._
  import CaseClass1ToValueClass._
  import ValidationGeneratorsTests._

  private def samples[T](n: Int)(implicit arbitrary: Arbitrary[T]): List[T] = List.fill(n)(arbitrary.arbitrary.sample).flatten

  test("values that a value class rejects are not generated") {
    val values = samples[Positive](500)
    values should not be empty
    all(values.map(_.value)) should be > 0
  }

  test("a value class's own from decides, also next to CaseClass1ToValueClass") {
    all(samples[Code](500).map(_.value.length)) shouldBe 3
  }
}
