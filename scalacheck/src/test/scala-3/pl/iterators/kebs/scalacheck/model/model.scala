package pl.iterators.kebs.scalacheck

import pl.iterators.kebs.opaque.Opaque

package object model {
  case class WrappedInt(int: Int)

  opaque type OpaqueInt = Int
  object OpaqueInt extends Opaque[OpaqueInt, Int] {
    override def apply(value: Int) = value
  }

  case class BasicSampleWithOpaque(
      someNumber: Int,
      someText: String,
      wrappedNumber: WrappedInt,
      opaqueInt: OpaqueInt
  )

}
