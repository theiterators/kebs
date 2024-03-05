package pl.iterators.kebs.core

import org.scalacheck.Prop.forAll
import org.scalacheck.Properties

object NumericSupportSpecification extends Properties("NumericSupport") {

  import NumbersDomain._
  import support._

  property("sum of List[TaggedBigDecimal]") = forAll { (bigDecimalList: List[BigDecimal]) =>
    bigDecimalList.map(TaggedBigDecimal(_)).sum == TaggedBigDecimal(bigDecimalList.sum)
  }

  property("sum of List[BoxedBigDecimal]") = forAll { (bigDecimalList: List[BigDecimal]) =>
    bigDecimalList.map(BoxedBigDecimal(_)).sum == BoxedBigDecimal(bigDecimalList.sum)
  }
}
