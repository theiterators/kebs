package pl.iterators.kebs.doobie

import pl.iterators.kebs.opaque.Opaque
import java.util.Currency
import pl.iterators.kebs.core.enums.ValueEnumLikeEntry

package object model {
  opaque type Name = String
  object Name extends Opaque[Name, String]

  enum EyeColor {
    case Blue, Green, Brown, Other
  }

  enum LibraryItem(val value: Int) extends ValueEnumLikeEntry[Int] {
    case Book     extends LibraryItem(1)
    case Movie    extends LibraryItem(2)
    case Magazine extends LibraryItem(3)
    case CD       extends LibraryItem(4)
  }

  case class Person(name: Name, eyeColor: EyeColor, preferredCurrency: Currency, relatives: List[Name], eyeballsInTheJar: Array[EyeColor])

}
