package pl.iterators.kebs.http4s.domain

import enumeratum.{Enum, EnumEntry}
import pl.iterators.kebs.tag.meta.tagged
import pl.iterators.kebs.tagged._

import java.util.UUID

@tagged trait Domain {
  trait AgeTag
  type Age = Int @@ AgeTag
  object Age {
    def validate(value: Int): Either[String, Int] =
      if (value < 0) Left("No going back, sorry") else Right(value)
  }
}

object Domain extends Domain {
  case class UserId(id: UUID)

  sealed trait Color extends EnumEntry
  object Color extends Enum[Color] {
    case object Red extends Color
    case object Blue extends Color
    case object Green extends Color

    override def values = findValues
  }
}