package pl.iterators.kebs_examples

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.{FromStringUnmarshaller, Unmarshaller}
import akka.http.scaladsl.util.FastFuture
import enumeratum.values._
import enumeratum.{Enum, EnumEntry}

object AkkaHttpUnmarshallers {
  sealed abstract class Column(val value: Int) extends IntEnumEntry
  object Column extends IntEnum[Column] {
    case object Name extends Column(1)
    case object Date extends Column(2)
    case object Type extends Column(3)

    override val values = findValues
  }

  sealed trait SortOrder extends EnumEntry
  object SortOrder extends Enum[SortOrder] {
    case object Asc  extends SortOrder
    case object Desc extends SortOrder

    override val values = findValues
  }

  case class Offset(value: Int) extends AnyVal
  case class Limit(value: Int)  extends AnyVal

  case class PaginationQuery(sortBy: Column, sortOrder: SortOrder, offset: Offset, limit: Limit)

  object BeforeKebs {
    final def enumUnmarshaller[E <: EnumEntry](`enum`: Enum[E]): FromStringUnmarshaller[E] = Unmarshaller { _ =>name =>
      `enum`.withNameInsensitiveOption(name) match {
        case Some(enumEntry) => FastFuture.successful(enumEntry)
        case None =>
          FastFuture.failed(new IllegalArgumentException(s"""Invalid value '$name'. Expected one of: ${`enum`.namesToValuesMap.keysIterator
            .mkString(", ")}"""))
      }
    }
    final def valueEnumUnmarshaller[V, E <: ValueEnumEntry[V]](`enum`: ValueEnum[V, E]): Unmarshaller[V, E] = Unmarshaller { _ =>v =>
      `enum`.withValueOpt(v) match {
        case Some(enumEntry) => FastFuture.successful(enumEntry)
        case None =>
          FastFuture.failed(new IllegalArgumentException(s"""Invalid value '$v'. Expected one of: ${`enum`.valuesToEntriesMap.keysIterator
            .mkString(", ")}"""))
      }
    }

    implicit val fromStringLimitUnmarshaller: FromStringUnmarshaller[Limit]         = Unmarshaller.intFromStringUnmarshaller map Limit
    implicit val fromStringOffsetUnmarshaller: FromStringUnmarshaller[Offset]       = Unmarshaller.intFromStringUnmarshaller map Offset
    implicit val fromStringSortOrderUnmarshaller: FromStringUnmarshaller[SortOrder] = enumUnmarshaller(SortOrder)
    implicit val fromStringColumnUnmarshaller
      : FromStringUnmarshaller[Column] = Unmarshaller.intFromStringUnmarshaller andThen valueEnumUnmarshaller(Column)

    val route = get {
      parameters(Symbol("sortBy").as[Column],
                 Symbol("order").as[SortOrder] ? (SortOrder.Desc: SortOrder),
                 Symbol("offset").as[Offset] ? Offset(0),
                 Symbol("limit").as[Limit])
        .as(PaginationQuery) { query =>
          complete(StatusCodes.OK)
        }
    }
  }

  object AfterKebs {
    import pl.iterators.kebs.unmarshallers._
    import enums._

    val route = get {
      parameters(Symbol("sortBy").as[Column],
                 Symbol("order").as[SortOrder] ? (SortOrder.Desc: SortOrder),
                 Symbol("offset").as[Offset] ? Offset(0),
                 Symbol("limit").as[Limit])
        .as(PaginationQuery) { query =>
          complete(StatusCodes.OK)
        }
    }
  }
}
