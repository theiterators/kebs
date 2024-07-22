package pl.iterators.kebs.core.support

import pl.iterators.kebs.core.macros.ValueClassLike

trait FractionalSupport {

  implicit def fractionalFromValueClassLike[A, Rep](implicit
      vcLike: ValueClassLike[A, Rep],
      fractionalRep: Fractional[Rep],
      numeric: Numeric[A]
  ): Fractional[A] =
    new Fractional[A] {
      override def div(x: A, y: A): A                  = vcLike.apply(fractionalRep.div(vcLike.unapply(x), vcLike.unapply(y)))
      override def plus(x: A, y: A): A                 = numeric.plus(x, y)
      override def minus(x: A, y: A): A                = numeric.minus(x, y)
      override def times(x: A, y: A): A                = numeric.times(x, y)
      override def negate(x: A): A                     = numeric.negate(x)
      override def fromInt(x: Int): A                  = numeric.fromInt(x)
      override def parseString(str: String): Option[A] = numeric.parseString(str)
      override def toInt(x: A): Int                    = numeric.toInt(x)
      override def toLong(x: A): Long                  = numeric.toLong(x)
      override def toFloat(x: A): Float                = numeric.toFloat(x)
      override def toDouble(x: A): Double              = numeric.toDouble(x)
      override def compare(x: A, y: A): Int            = numeric.compare(x, y)
    }

}
