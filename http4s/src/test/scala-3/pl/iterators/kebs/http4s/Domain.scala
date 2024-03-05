package pl.iterators.kebs.http4s

import pl.iterators.kebs.opaque.Opaque

import java.util.UUID

object Domain {
  opaque type Age = Int
  object Age extends Opaque[Age, Int] {
    override def validate(value: Int): Either[String, Age] =
      if (value < 0) Left("No going back, sorry") else Right(value)
  }

  case class UserId(id: UUID)

  enum Color {
    case Red, Blue, Green
  }
}