package pl.iterators.kebs.doobie

import enumeratum.values.{IntEnum, IntEnumEntry}
import enumeratum.{Enum, EnumEntry}
import pl.iterators.kebs.core.enums.ValueEnumLikeEntry

import java.util.Currency

package object model {

  case class Name(name: String)

  sealed trait EyeColor extends EnumEntry

  object EyeColor extends Enum[EyeColor] {
    case object Blue  extends EyeColor
    case object Green extends EyeColor
    case object Brown extends EyeColor
    case object Other extends EyeColor

    def values = findValues
  }

  sealed abstract class LibraryItem(val value: Int) extends IntEnumEntry with ValueEnumLikeEntry[Int]

  object LibraryItem extends IntEnum[LibraryItem] {
    case object Book     extends LibraryItem(1)
    case object Movie    extends LibraryItem(2)
    case object Magazine extends LibraryItem(3)
    case object CD       extends LibraryItem(4)

    val values = findValues
  }

  case class Person(name: Name, eyeColor: EyeColor, preferredCurrency: Currency, relatives: List[Name], eyeballsInTheJar: Array[EyeColor])

}
