package pl.iterators.kebs.support

import pl.iterators.kebs.macros.ValueClassLike

trait NumericSupport {

  implicit def numericFromValueClassLike[A, Rep](implicit vcLike: ValueClassLike[A, Rep], numericRep: Numeric[Rep]): Numeric[A] = {
    new Numeric[A] {
      override def plus(x: A, y: A): A                 = vcLike.apply(numericRep.plus(vcLike.unapply(x), vcLike.unapply(y)))
      override def minus(x: A, y: A): A                = vcLike.apply(numericRep.minus(vcLike.unapply(x), vcLike.unapply(y)))
      override def times(x: A, y: A): A                = vcLike.apply(numericRep.times(vcLike.unapply(x), vcLike.unapply(y)))
      override def negate(x: A): A                     = vcLike.apply(numericRep.negate(vcLike.unapply(x)))
      override def fromInt(x: Int): A                  = vcLike.apply(numericRep.fromInt(x))
      override def toInt(x: A): Int                    = numericRep.toInt(vcLike.unapply(x))
      override def toLong(x: A): Long                  = numericRep.toLong(vcLike.unapply(x))
      override def toFloat(x: A): Float                = numericRep.toFloat(vcLike.unapply(x))
      override def toDouble(x: A): Double              = numericRep.toDouble(vcLike.unapply(x))
      override def compare(x: A, y: A): Int            = numericRep.compare(vcLike.unapply(x), vcLike.unapply(y))
      override def parseString(str: String): Option[A] = numericRep.parseString(str).map(vcLike.apply)
    }
  }

}
