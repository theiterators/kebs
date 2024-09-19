package pl.iterators.kebs.scalacheck

import org.scalacheck._
import io.github.martinhh.derived.scalacheck._
import scala.deriving.Mirror

trait ScalacheckInstancesSupport {
  inline implicit def arb[T](using inline mirror: Mirror.Of[T]): Arbitrary[T] = deriveArbitrary[T]
}
