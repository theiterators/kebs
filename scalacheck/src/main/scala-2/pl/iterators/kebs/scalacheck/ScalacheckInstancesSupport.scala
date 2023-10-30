package pl.iterators.kebs.scalacheck

import org.scalacheck._

trait ScalacheckInstancesSupport {
implicit def arb[T]: Arbitrary[T] = macro magnolify.scalacheck.auto.genArbitraryMacro[T]
}