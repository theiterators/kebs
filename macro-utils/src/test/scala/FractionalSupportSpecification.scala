import org.scalacheck.Prop.forAll
import org.scalacheck.{Arbitrary, Properties}

object FractionalSupportSpecification extends Properties("FractionalSupport") {

  private val nonZeroBigDecimal = Arbitrary.arbitrary[BigDecimal] suchThat (_ != 0)

  import NumbersDomain._
  import pl.iterators.kebs.support._

  private def divide[A: Fractional](f1: A, f2: A): Option[A] = {
    import Fractional.Implicits._
    try {
      Some(f1 / f2)
    } catch {
      case _: ArithmeticException => None
    }
  }

  private def divideBigDecimals(f1: BigDecimal, f2: BigDecimal): Option[BigDecimal] = {
    try {
      Some(f1 / f2)
    } catch {
      case _: ArithmeticException => None
    }
  }

  property("Fractional[TaggedBigDecimal]") = forAll(Arbitrary.arbitrary[BigDecimal], nonZeroBigDecimal) { (dividend, divisor) =>
    val quotient = divideBigDecimals(dividend, divisor)
    divide(TaggedBigDecimal(dividend), TaggedBigDecimal(divisor)) == quotient.map(TaggedBigDecimal(_))
  }

  property("Fractional[BoxedBigDecimal]") = forAll(Arbitrary.arbitrary[BigDecimal], nonZeroBigDecimal) { (dividend, divisor) =>
    val quotient = divideBigDecimals(dividend, divisor)
    divide(BoxedBigDecimal(dividend), BoxedBigDecimal(divisor)) == quotient.map(BoxedBigDecimal(_))
  }
}
