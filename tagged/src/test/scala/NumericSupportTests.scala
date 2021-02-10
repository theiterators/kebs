import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.macros.CaseClass1Rep
import pl.iterators.kebs.tagged._
import pl.iterators.kebs.tagged.implicits.NumericSupport

class NumericSupportTests extends AnyFunSuite with Matchers with NumericSupport {

  trait Tag
  type TaggedNumber = BigDecimal @@ Tag
  object TaggedNumber {
    def apply(value: BigDecimal): TaggedNumber = value.@@[Tag]
  }
  object Tag {
    implicit val TaggedNumberCaseClass1Rep: CaseClass1Rep[TaggedNumber, BigDecimal] =
      new CaseClass1Rep[TaggedNumber, BigDecimal](TaggedNumber.apply, identity)
  }

  test("sum should be available for numeric tagged types") {
    val list = List(TaggedNumber(10), TaggedNumber(20))
    list.sum shouldBe TaggedNumber(30)
  }

  test("ordering should be available for numeric tagged types") {
    val list = List(TaggedNumber(10), TaggedNumber(20))
    list.sorted shouldBe List(TaggedNumber(10), TaggedNumber(20))
    list.sorted(Ordering[TaggedNumber].reverse) shouldBe List(TaggedNumber(20), TaggedNumber(10))
  }

  case class BoxedNumber(value: BigDecimal)
  object BoxedNumber {
    implicit val BoxedNumberCaseClass1Rep: CaseClass1Rep[BoxedNumber, BigDecimal] =
      new CaseClass1Rep[BoxedNumber, BigDecimal](BoxedNumber.apply, _.value)
  }

  test("sum should be available for numeric boxed types") {
    val list = List(BoxedNumber(10), BoxedNumber(20))
    list.sum shouldBe BoxedNumber(30)
  }

  test("ordering should be available for numeric boxed types") {
    val list = List(BoxedNumber(10), BoxedNumber(20))
    list.sorted shouldBe List(BoxedNumber(10), BoxedNumber(20))
    list.sorted(Ordering[BoxedNumber].reverse) shouldBe List(BoxedNumber(20), BoxedNumber(10))
  }
}
