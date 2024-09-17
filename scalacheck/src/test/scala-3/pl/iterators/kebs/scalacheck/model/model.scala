package pl.iterators.kebs.scalacheck

import pl.iterators.kebs.opaque.Opaque
import pl.iterators.kebs.core.enums.ValueEnumLikeEntry
import java.time._

package object model {
  enum Greeting {
    case Hello, GoodBye, Hi, Bye
  }

  enum LongGreeting(val value: Long) extends ValueEnumLikeEntry[Long] {
    case Hello   extends LongGreeting(0L)
    case GoodBye extends LongGreeting(1L)
    case Hi      extends LongGreeting(2L)
    case Bye     extends LongGreeting(3L)
  }

  opaque type WrappedInt = Int
  object WrappedInt extends Opaque[WrappedInt, Int] {
    override def apply(value: Int) = value
  }

  case class BasicSample(
      someNumber: Int,
      someText: String,
      wrappedNumber: WrappedInt,
      opaqueInt: WrappedInt,
      greeting: Greeting,
      longGreeting: LongGreeting
  )
}
