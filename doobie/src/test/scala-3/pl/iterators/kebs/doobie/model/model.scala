package pl.iterators.kebs.doobie

import pl.iterators.kebs.opaque.Opaque
import java.util.Currency

package object model {
  opaque type Name = String
  object Name extends Opaque[Name, String]

  enum EyeColor {
    case Blue, Green, Brown, Other
  }
  
  case class Person(name: Name, eyeColor: EyeColor, preferredCurrency: Currency, relatives: List[Name], eyeballsInTheJar: Array[EyeColor])

}
