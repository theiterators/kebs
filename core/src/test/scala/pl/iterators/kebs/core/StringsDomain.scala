package pl.iterators.kebs.core

import pl.iterators.kebs.core.macros.ValueClassLike

object StringsDomain {
  trait Tag1
  type TaggedString = String with Tag1
  object TaggedString {
    def apply(value: String): TaggedString = value.asInstanceOf[TaggedString]
  }
  object Tag1 {
    implicit val taggedStringValueClassLike: ValueClassLike[TaggedString, String] =
      new ValueClassLike[TaggedString, String](TaggedString.apply, identity)
  }

  case class BoxedString(value: String)
  object BoxedString {
    implicit val boxedStringValueClassLike: ValueClassLike[BoxedString, String] =
      new ValueClassLike[BoxedString, String](BoxedString.apply, _.value)
  }
}
