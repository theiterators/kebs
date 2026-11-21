package pl.iterators.kebs.enums

import pl.iterators.kebs.core.enums.EnumLike

/** A type class with an instance that needs EnumLike and a low-priority fallback: a type without an EnumLike must get the fallback, never
  * a compile error.
  */
final class PickEnumLike[A](val chosen: String)
trait LowPriorityPickEnumLike {
  implicit def fallback[A]: PickEnumLike[A] = new PickEnumLike[A]("fallback")
}
object PickEnumLike extends LowPriorityPickEnumLike {
  implicit def enumBased[A](implicit e: EnumLike[A]): PickEnumLike[A] = new PickEnumLike[A]("enum")
}
