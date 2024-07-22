package pl.iterators.kebs.scalacheck

package object model {
  case class WrappedInt(int: Int)
  case class WrappedIntAnyVal(int: Int) extends AnyVal
  case class BasicSample(
      someNumber: Int,
      someText: String,
      wrappedNumber: WrappedInt,
      wrappedNumberAnyVal: WrappedIntAnyVal
  )
}
