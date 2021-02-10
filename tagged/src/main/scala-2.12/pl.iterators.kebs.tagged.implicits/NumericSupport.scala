package pl.iterators.kebs.tagged.implicits

import pl.iterators.kebs.macros.CaseClass1Rep

trait NumericSupport {

  implicit def numericImpl[T, A](implicit rep: CaseClass1Rep[T, A], n: Numeric[A]): Numeric[T] = {
    new Numeric[T] {
      override def plus(x: T, y: T): T                 = rep.apply(n.plus(rep.unapply(x), rep.unapply(y)))
      override def minus(x: T, y: T): T                = rep.apply(n.minus(rep.unapply(x), rep.unapply(y)))
      override def times(x: T, y: T): T                = rep.apply(n.times(rep.unapply(x), rep.unapply(y)))
      override def negate(x: T): T                     = rep.apply(n.negate(rep.unapply(x)))
      override def fromInt(x: Int): T                  = rep.apply(n.fromInt(x))
      override def toInt(x: T): Int                    = n.toInt(rep.unapply(x))
      override def toLong(x: T): Long                  = n.toLong(rep.unapply(x))
      override def toFloat(x: T): Float                = n.toFloat(rep.unapply(x))
      override def toDouble(x: T): Double              = n.toDouble(rep.unapply(x))
      override def compare(x: T, y: T): Int            = n.compare(rep.unapply(x), rep.unapply(y))
    }
  }

}
