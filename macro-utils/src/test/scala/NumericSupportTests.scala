import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class NumericSupportTests extends AnyFunSuite with Matchers {

  import pl.iterators.kebs.support._
  import NumbersDomain._

  test("sum should be available for numeric tagged types") {
    val list = List(TaggedBigDecimal(10), TaggedBigDecimal(20))
    list.sum shouldBe TaggedBigDecimal(30)
  }

  test("sum should be available for numeric boxed types") {
    val list = List(BoxedBigDecimal(10), BoxedBigDecimal(20))
    list.sum shouldBe BoxedBigDecimal(30)
  }

}
