import pl.iterators.kebs.macros.base.CaseClass1Rep

object NumbersDomain {

  trait Tag1
  type TaggedBigDecimal = BigDecimal with Tag1
  object TaggedBigDecimal {
    def apply(value: BigDecimal): TaggedBigDecimal = value.asInstanceOf[TaggedBigDecimal]
  }
  object Tag1 {
    implicit val TaggedBigDecimalCaseClass1Rep: CaseClass1Rep[TaggedBigDecimal, BigDecimal] =
      new CaseClass1Rep[TaggedBigDecimal, BigDecimal](TaggedBigDecimal.apply, identity)
  }

  case class BoxedBigDecimal(value: BigDecimal)
  object BoxedBigDecimal {
    implicit val BoxedBigDecimalCaseClass1Rep: CaseClass1Rep[BoxedBigDecimal, BigDecimal] =
      new CaseClass1Rep[BoxedBigDecimal, BigDecimal](BoxedBigDecimal.apply, _.value)
  }

  trait Tag2
  type TaggedInt = Int with Tag2
  object TaggedInt {
    def apply(value: Int): TaggedInt = value.asInstanceOf[TaggedInt]
  }
  object Tag2 {
    implicit val TaggedIntCaseClass1Rep: CaseClass1Rep[TaggedInt, Int] =
      new CaseClass1Rep[TaggedInt, Int](TaggedInt.apply, identity)
  }

  case class BoxedInt(value: Int)
  object BoxedInt {
    implicit val BoxedIntCaseClass1Rep: CaseClass1Rep[BoxedInt, Int] =
      new CaseClass1Rep[BoxedInt, Int](BoxedInt.apply, _.value)
  }
}
