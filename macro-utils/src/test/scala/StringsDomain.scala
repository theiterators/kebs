import pl.iterators.kebs.macros.base.CaseClass1Rep

object StringsDomain {
  trait Tag1
  type TaggedString = String with Tag1
  object TaggedString {
    def apply(value: String): TaggedString = value.asInstanceOf[TaggedString]
  }
  object Tag1 {
    implicit val TaggedStringCaseClass1Rep: CaseClass1Rep[TaggedString, String] =
      new CaseClass1Rep[TaggedString, String](TaggedString.apply, identity)
  }

  case class BoxedString(value: String)
  object BoxedString {
    implicit val BoxedStringCaseClass1Rep: CaseClass1Rep[BoxedString, String] =
      new CaseClass1Rep[BoxedString, String](BoxedString.apply, _.value)
  }
}
