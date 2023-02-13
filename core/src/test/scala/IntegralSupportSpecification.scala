import org.scalacheck.Prop.forAll
import org.scalacheck.{Arbitrary, Properties}

object IntegralSupportSpecification extends Properties("IntegralSupport") {

  private val nonZeroInt = Arbitrary.arbitrary[Int] suchThat (_ != 0)

  import NumbersDomain._
  import pl.iterators.kebs.support._

  private def divide[A: Integral](f1: A, f2: A): (A, A) = {
    import Integral.Implicits._
    f1 /% f2
  }

  property("Integral[TaggedInt]") = forAll(Arbitrary.arbitrary[Int], nonZeroInt) { (dividend, divisor) =>
    val quotient  = dividend / divisor
    val remainder = dividend % divisor
    divide(TaggedInt(dividend), TaggedInt(divisor)) == (TaggedInt(quotient), TaggedInt(remainder))
  }

  property("Integral[BoxedInt]") = forAll(Arbitrary.arbitrary[Int], nonZeroInt) { (dividend, divisor) =>
    val quotient  = dividend / divisor
    val remainder = dividend % divisor
    divide(BoxedInt(dividend), BoxedInt(divisor)) == (BoxedInt(quotient), BoxedInt(remainder))
  }
}
