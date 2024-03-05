package pl.iterators.kebs

import pl.iterators.kebs.opaque.Opaque

import java.net.URI
import java.util.UUID

object Domain {
  opaque type TestTaggedUri = URI
  object TestTaggedUri extends Opaque[TestTaggedUri, URI]
  opaque type TestId = UUID
  object TestId extends Opaque[TestId, UUID]
  opaque type Id = Long
  object Id extends Opaque[Id, Long]
  case class I(i: Int)
  case class S(s: String)
  case class P[A](a: A)
  case class CantUnmarshall(s: String, i: Int)
  case object O

  enums Greeting {
    case Hello, GoodBye, Hi, Bye
  }


  enums LibraryItem(val value: Int) extends ValueEnumLikeEntry[Int] {
    case Book extends LibraryItem(1)
    case Movie extends LibraryItem(2)
    case Magazine extends LibraryItem(3)
    case CD extends LibraryItem(4)
  }

  case class Red(value: Int)
  case class Green(value: Int)
  case class Blue(value: Int)
  case class Color(red: Red, green: Green, blue: Blue)

  enums ShirtSize(val value: String) extends ValueEnumLikeEntry[String] {
    case Small extends ShirtSize("S")
    case Medium extends ShirtSize("M")
    case Large extends ShirtSize("L")
  }

  enums SortOrder {
    case Asc
    case Desc
  }

}
