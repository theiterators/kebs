import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class FractionalSupportTests extends AnyFunSuite with Matchers {

  import pl.iterators.kebs.support._
  import NumbersDomain._

  private def divide[A: Fractional](f1: A, f2: A): A = {
    import Fractional.Implicits._
    f1 / f2
  }

  test("Fractional should be available for tagged types with Fractional implementation") {
    divide(TaggedBigDecimal(100), TaggedBigDecimal(5)) shouldBe TaggedBigDecimal(20)
  }

  test("Fractional should be available for boxed types with Fractional implementation") {
    divide(BoxedBigDecimal(100), BoxedBigDecimal(5)) shouldBe BoxedBigDecimal(20)
  }

}
