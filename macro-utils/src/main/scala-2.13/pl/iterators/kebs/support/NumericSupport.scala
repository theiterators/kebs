package pl.iterators.kebs.support

import pl.iterators.kebs.macros.base.CaseClass1Rep

trait NumericSupport {

  implicit def numericFromCaseClass1Rep[A, Rep](implicit cc1Rep: CaseClass1Rep[A, Rep], numericRep: Numeric[Rep]): Numeric[A] = {
    new Numeric[A] {
      override def plus(x: A, y: A): A                 = cc1Rep.apply(numericRep.plus(cc1Rep.unapply(x), cc1Rep.unapply(y)))
      override def minus(x: A, y: A): A                = cc1Rep.apply(numericRep.minus(cc1Rep.unapply(x), cc1Rep.unapply(y)))
      override def times(x: A, y: A): A                = cc1Rep.apply(numericRep.times(cc1Rep.unapply(x), cc1Rep.unapply(y)))
      override def negate(x: A): A                     = cc1Rep.apply(numericRep.negate(cc1Rep.unapply(x)))
      override def fromInt(x: Int): A                  = cc1Rep.apply(numericRep.fromInt(x))
      override def toInt(x: A): Int                    = numericRep.toInt(cc1Rep.unapply(x))
      override def toLong(x: A): Long                  = numericRep.toLong(cc1Rep.unapply(x))
      override def toFloat(x: A): Float                = numericRep.toFloat(cc1Rep.unapply(x))
      override def toDouble(x: A): Double              = numericRep.toDouble(cc1Rep.unapply(x))
      override def compare(x: A, y: A): Int            = numericRep.compare(cc1Rep.unapply(x), cc1Rep.unapply(y))
      override def parseString(str: String): Option[A] = numericRep.parseString(str).map(cc1Rep.apply)
    }
  }

}
