package pl.iterators.kebs

// import pl.iterators.kebs.tag.meta.tagged
// import pl.iterators.kebs.tagged._

import java.net.URI
import java.util.UUID

// @tagged trait Tags {
//   trait IdTag
//   type Id = Long @@ IdTag

//   trait TestIdTag
//   type TestId = UUIDId @@ TestIdTag

//   trait TestDoubleTag
//   type TestDouble = Double @@ TestDoubleTag

//   type UUIDId = UUID
//   object UUIDId {
//     def generate[T]: UUIDId @@ T = UUID.randomUUID().taggedWith[T]
//     def fromString[T](str: String): UUIDId @@ T =
//       UUID.fromString(str).taggedWith[T]
//   }

//   trait TestTaggedUriTag
//   type TestTaggedUri = URI @@ TestTaggedUriTag

// }

object Domain {
  case class I(i: Int)
  case class S(s: String)
  case class P[A](a: A)
  case class CantUnmarshall(s: String, i: Int)
  case object O

  enum Greeting {
    case Hello, GoodBye, Hi, Bye
  }

  enum LibraryItem(val i: Int):
    case Book extends LibraryItem(1)
    case Movie extends LibraryItem(2)
    case Magazine extends LibraryItem(3)
    case CD extends LibraryItem(4)

  case class Red(value: Int)
  case class Green(value: Int)
  case class Blue(value: Int)
  case class Color(red: Red, green: Green, blue: Blue)

  enum ShirtSize(val s: String):
    case Small  extends ShirtSize("S")
    case Medium extends ShirtSize("M")
    case Large  extends ShirtSize("L")

  enum SortOrder {
    case Asc, Desc
  }

}
