package pl.iterators.kebs.doobie

import enumeratum.{Enum, EnumEntry}
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

  case class Person(name: Name, eyeColor: EyeColor, preferredCurrency: Currency, relatives: List[Name], eyeballsInTheJar: Array[EyeColor])

}
