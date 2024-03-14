package pl.iterators.kebs.examples

import pl.iterators.kebs.tag.meta.tagged
import pl.iterators.kebs.tagged._

@tagged trait NumericDomain {
  trait Tag1
  type TaggedNumber = BigDecimal @@ Tag1
}

object Domain extends NumericDomain

object NumericExample {
  import Domain._

  val sumTagged = List(TaggedNumber(10), TaggedNumber(20)).sum
  // sumTagged: TaggedNumber = 30

  case class BoxedNumber(n: BigDecimal)
  val sumBoxed = List(BoxedNumber(10), BoxedNumber(20)).sum
  // sumBoxed: BoxedNumber = 30
}
