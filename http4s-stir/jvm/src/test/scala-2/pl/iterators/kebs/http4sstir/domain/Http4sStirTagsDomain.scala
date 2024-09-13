package pl.iterators.kebs.http4sstir.domain

import enumeratum.values.{IntEnum, IntEnumEntry, StringEnum, StringEnumEntry}
import enumeratum.{Enum, EnumEntry}
import pl.iterators.kebs.tag.meta.tagged
import pl.iterators.kebs.tagged._
import pl.iterators.kebs.core.enums.ValueEnumLikeEntry

import java.net.URI
import java.util.UUID

@tagged trait Tags {
  trait IdTag
  type Id = Long @@ IdTag

  trait TestIdTag
  type TestId = UUIDId @@ TestIdTag

  trait TestDoubleTag
  type TestDouble = Double @@ TestDoubleTag

  type UUIDId = UUID
  object UUIDId {
    def generate[T]: UUIDId @@ T = UUID.randomUUID().taggedWith[T]
    def fromString[T](str: String): UUIDId @@ T =
      UUID.fromString(str).taggedWith[T]
  }

  trait TestTaggedUriTag
  type TestTaggedUri = URI @@ TestTaggedUriTag

}

object Domain extends Tags {
  case class I(i: Int)
  case class S(s: String)
  case class P[A](a: A)
  case class CantUnmarshall(s: String, i: Int)
  case object O

  sealed trait Greeting extends EnumEntry
  object Greeting extends Enum[Greeting] {
    case object Hello   extends Greeting
    case object GoodBye extends Greeting
    case object Hi      extends Greeting
    case object Bye     extends Greeting

    val values = findValues
  }

  sealed abstract class LibraryItem(val value: Int) extends IntEnumEntry with ValueEnumLikeEntry[Int]

  object LibraryItem extends IntEnum[LibraryItem] {
    case object Book     extends LibraryItem(1)
    case object Movie    extends LibraryItem(2)
    case object Magazine extends LibraryItem(3)
    case object CD       extends LibraryItem(4)

    val values = findValues
  }

  case class Red(value: Int)
  case class Green(value: Int)
  case class Blue(value: Int)
  case class Color(red: Red, green: Green, blue: Blue)

  sealed abstract class ShirtSize(val value: String) extends StringEnumEntry with ValueEnumLikeEntry[String]
  object ShirtSize extends StringEnum[ShirtSize] {
    case object Small  extends ShirtSize("S")
    case object Medium extends ShirtSize("M")
    case object Large  extends ShirtSize("L")

    val values = findValues
  }

  sealed trait SortOrder extends EnumEntry
  object SortOrder extends Enum[SortOrder] {
    case object Asc  extends SortOrder
    case object Desc extends SortOrder

    override val values = findValues
  }

}
