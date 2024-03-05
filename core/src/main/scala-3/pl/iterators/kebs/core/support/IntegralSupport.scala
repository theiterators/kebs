package pl.iterators.kebs.support

import pl.iterators.kebs.macros.ValueClassLike

trait IntegralSupport {

  implicit def integralFromValueClassLike[A, Rep](implicit vcLike: ValueClassLike[A, Rep],
                                                 integralRep: Integral[Rep],
                                                 numeric: Numeric[A]): Integral[A] =
    new Integral[A] {
      override def quot(x: A, y: A): A                 = vcLike.apply(integralRep.quot(vcLike.unapply(x), vcLike.unapply(y)))
      override def rem(x: A, y: A): A                  = vcLike.apply(integralRep.rem(vcLike.unapply(x), vcLike.unapply(y)))
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
