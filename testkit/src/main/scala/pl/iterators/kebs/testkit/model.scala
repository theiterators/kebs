package pl.iterators.kebs.testkit

import enumeratum.values.{IntEnum, IntEnumEntry}
import enumeratum.{Enum, EnumEntry}
import pl.iterators.kebs.core.enums.ValueEnumLikeEntry

import java.time.Instant

// a separate module on purpose: derivation must also work for types read from the classpath
object model {
  final case class Id(value: String)
  final case class Person(id: Id, name: String, age: Int, nickname: Option[String])
  final case class Naming(someField: Int, anotherFieldName: String, field1Name: Int, httpURL: String)
  final case class Event(id: Id, at: Instant)

  final case class Email private (value: String)
  object Email {
    def from(value: String): Email = new Email(value.toLowerCase)
  }
  final case class Contact(email: Email, name: String)

  final case class Wide(
      f1: Int,
      f2: Int,
      f3: Int,
      f4: Int,
      f5: Int,
      f6: Int,
      f7: Int,
      f8: Int,
      f9: Int,
      f10: Int,
      f11: Int,
      f12: Int,
      f13: Int,
      f14: Int,
      f15: Int,
      f16: Int,
      f17: Int,
      f18: Int,
      f19: Int,
      f20: Int,
      f21: Int,
      f22: Int,
      f23: Option[String]
  )
  object Wide {
    val sample: Wide = Wide(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, None)
  }

  sealed abstract class Color extends EnumEntry
  object Color                extends Enum[Color] {
    case object Red      extends Color
    case object DarkBlue extends Color
    val values = findValues
  }

  sealed abstract class Level(val value: Int) extends IntEnumEntry with ValueEnumLikeEntry[Int]
  object Level                                extends IntEnum[Level] {
    case object Low  extends Level(1)
    case object High extends Level(2)
    val values = findValues
  }

  final case class Paint(color: Color, level: Level)

  sealed trait Shape
  final case class Circle(r: Double, label: String) extends Shape
  case object Empty                                 extends Shape
}
