package pl.iterators.kebs.scalacheck

import pl.iterators.kebs.opaque.Opaque
import pl.iterators.kebs.core.enums.ValueEnumLikeEntry

package object model {
  case class WrappedInt(int: Int)

  enum Greeting {
    case Hello, GoodBye, Hi, Bye
  }

  enum LongGreeting(val value: Long) extends ValueEnumLikeEntry[Long] {
    case Hello   extends LongGreeting(0L)
    case GoodBye extends LongGreeting(1L)
    case Hi      extends LongGreeting(2L)
    case Bye     extends LongGreeting(3L)
  }

  opaque type OpaqueInt = Int
  object OpaqueInt extends Opaque[OpaqueInt, Int] {
    override def apply(value: Int) = value
  }

  case class BasicSampleWithOpaque(
      someNumber: Int,
      someText: String,
      wrappedNumber: WrappedInt,
      opaqueInt: OpaqueInt,
      greeting: Greeting,
      longGreeting: LongGreeting
  )

}
