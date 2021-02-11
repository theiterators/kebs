import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class OrderingSupportTests extends AnyFunSuite with Matchers {

  import pl.iterators.kebs.support._
  import NumbersDomain._

  test("ordering should be available for numeric tagged types") {
    val list = List(TaggedBigDecimal(10), TaggedBigDecimal(20))
    list.sorted shouldBe List(TaggedBigDecimal(10), TaggedBigDecimal(20))
    list.sorted(Ordering[TaggedBigDecimal].reverse) shouldBe List(TaggedBigDecimal(20), TaggedBigDecimal(10))
  }

  test("ordering should be available for numeric boxed types") {
    val list = List(BoxedBigDecimal(10), BoxedBigDecimal(20))
    list.sorted shouldBe List(BoxedBigDecimal(10), BoxedBigDecimal(20))
    list.sorted(Ordering[BoxedBigDecimal].reverse) shouldBe List(BoxedBigDecimal(20), BoxedBigDecimal(10))
  }

}
