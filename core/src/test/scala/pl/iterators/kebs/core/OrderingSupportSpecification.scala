package pl.iterators.kebs.core

import org.scalacheck.Prop.forAll
import org.scalacheck.Properties

object OrderingSupportSpecification extends Properties("OrderingSupport") {

  import support._
  import NumbersDomain._

  private def toTagged(list: List[BigDecimal]): List[TaggedBigDecimal] =
    list.map(TaggedBigDecimal(_))

  private def toBoxed(list: List[BigDecimal]): List[BoxedBigDecimal] =
    list.map(BoxedBigDecimal(_))

  private def sortedReverse[A: Ordering](list: List[A]): List[A] =
    list.sorted(Ordering[A].reverse)

  property("ordering should be available for numeric tagged types") = forAll { (bigDecimalList: List[BigDecimal]) =>
    toTagged(bigDecimalList).sorted == toTagged(bigDecimalList.sorted)
  }

  property("reverse ordering should be available for numeric tagged types") = forAll { (bigDecimalList: List[BigDecimal]) =>
    sortedReverse(toTagged(bigDecimalList)) == toTagged(sortedReverse(bigDecimalList))
  }

  property("ordering should be available for numeric boxed types") = forAll { (bigDecimalList: List[BigDecimal]) =>
    toBoxed(bigDecimalList).sorted == toBoxed(bigDecimalList.sorted)
  }

  property("reverse ordering should be available for numeric boxed types") = forAll { (bigDecimalList: List[BigDecimal]) =>
    sortedReverse(toBoxed(bigDecimalList)) == toBoxed(sortedReverse(bigDecimalList))
  }
}
