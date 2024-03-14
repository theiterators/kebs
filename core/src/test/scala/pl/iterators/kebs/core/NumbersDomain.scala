package pl.iterators.kebs.core

import pl.iterators.kebs.core.macros.ValueClassLike

object NumbersDomain {

  trait Tag1
  type TaggedBigDecimal = BigDecimal with Tag1
  object TaggedBigDecimal {
    def apply(value: BigDecimal): TaggedBigDecimal = value.asInstanceOf[TaggedBigDecimal]
  }
  object Tag1 {
    implicit val taggedBigDecimalValueClassLike: ValueClassLike[TaggedBigDecimal, BigDecimal] =
      new ValueClassLike[TaggedBigDecimal, BigDecimal](TaggedBigDecimal.apply, identity)
  }

  case class BoxedBigDecimal(value: BigDecimal)
  object BoxedBigDecimal {
    implicit val boxedBigDecimalValueClassLike: ValueClassLike[BoxedBigDecimal, BigDecimal] =
      new ValueClassLike[BoxedBigDecimal, BigDecimal](BoxedBigDecimal.apply, _.value)
  }

  trait Tag2
  type TaggedInt = Int with Tag2
  object TaggedInt {
    def apply(value: Int): TaggedInt = value.asInstanceOf[TaggedInt]
  }
  object Tag2 {
    implicit val taggedIntValueClassLike: ValueClassLike[TaggedInt, Int] =
      new ValueClassLike[TaggedInt, Int](TaggedInt.apply, identity)
  }

  case class BoxedInt(value: Int)
  object BoxedInt {
    implicit val boxedIntValueClassLike: ValueClassLike[BoxedInt, Int] =
      new ValueClassLike[BoxedInt, Int](BoxedInt.apply, _.value)
  }
}
