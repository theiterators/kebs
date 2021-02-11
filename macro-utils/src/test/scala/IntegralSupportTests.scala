import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class IntegralSupportTests extends AnyFunSuite with Matchers {

  import NumbersDomain._
  import pl.iterators.kebs.support._

  private def divide[A: Integral](f1: A, f2: A): (A, A) = {
    import Integral.Implicits._
    f1 /% f2
  }

  test("Integral should be available for tagged types with Integral implementation") {
    divide(TaggedInt(100), TaggedInt(30)) shouldBe (TaggedInt(3), TaggedInt(10))
  }

  test("Integral should be available for boxed types with Integral implementation") {
    divide(BoxedInt(100), BoxedInt(30)) shouldBe (BoxedInt(3), BoxedInt(10))
  }

}
