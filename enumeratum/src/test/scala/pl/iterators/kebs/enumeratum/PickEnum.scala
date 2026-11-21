package pl.iterators.kebs.enumeratum

import pl.iterators.kebs.core.enums.EnumLike

// enum codecs are searched like this: no EnumLike must mean the fallback, never a compile error
final class PickEnum[A](val chosen: String)
trait LowPriorityPickEnum {
  implicit def fallback[A]: PickEnum[A] = new PickEnum[A]("fallback")
}
object PickEnum extends LowPriorityPickEnum {
  implicit def enumBased[A](implicit e: EnumLike[A]): PickEnum[A] = new PickEnum[A]("enum")
}
