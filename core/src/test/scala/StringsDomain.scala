import pl.iterators.kebs.macros.ValueClassLike

object StringsDomain {
  trait Tag1
  type TaggedString = String with Tag1
  object TaggedString {
    def apply(value: String): TaggedString = value.asInstanceOf[TaggedString]
  }
  object Tag1 {
    implicit val TaggedStringCaseClass1Rep: ValueClassLike[TaggedString, String] =
      new ValueClassLike[TaggedString, String](TaggedString.apply, identity)
  }

  case class BoxedString(value: String)
  object BoxedString {
    implicit val BoxedStringCaseClass1Rep: ValueClassLike[BoxedString, String] =
      new ValueClassLike[BoxedString, String](BoxedString.apply, _.value)
  }
}
