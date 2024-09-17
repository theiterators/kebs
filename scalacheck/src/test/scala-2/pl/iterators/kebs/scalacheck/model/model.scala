package pl.iterators.kebs.scalacheck

import enumeratum.{Enum, EnumEntry}
import enumeratum.values.{LongEnum, LongEnumEntry}
import pl.iterators.kebs.core.enums.ValueEnumLikeEntry

package object model {
  sealed abstract class LongGreeting(val value: Long) extends LongEnumEntry with ValueEnumLikeEntry[Long]

  object LongGreeting extends LongEnum[LongGreeting] {
    val values = findValues

    case object Hello   extends LongGreeting(0L)
    case object GoodBye extends LongGreeting(1L)
    case object Hi      extends LongGreeting(2L)
    case object Bye     extends LongGreeting(3L)
  }

  sealed trait Greeting extends EnumEntry

  object Greeting extends Enum[Greeting] {
    val values = findValues

    case object Hello   extends Greeting
    case object GoodBye extends Greeting
    case object Hi      extends Greeting
    case object Bye     extends Greeting
  }

  case class WrappedInt(int: Int)
  case class WrappedIntAnyVal(int: Int) extends AnyVal

  case class BasicSample(
      someNumber: Int,
      someText: String,
      wrappedNumber: WrappedInt,
      wrappedNumberAnyVal: WrappedIntAnyVal,
      greeting: Greeting,
      longGreeting: LongGreeting
  )
}
