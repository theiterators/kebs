package pl.iterators.kebs.http4sstir.unmarshallers.enums

import cats.effect.IO
import pl.iterators.stir.unmarshalling.PredefinedFromStringUnmarshallers.*
import pl.iterators.stir.unmarshalling.{FromStringUnmarshaller, Unmarshaller}

import scala.reflect.{ClassTag, Enum}
import scala.reflect.Selectable.reflectiveSelectable

import pl.iterators.kebs.core.enums.{EnumLike, ValueEnumLike, ValueEnumLikeEntry}

trait EnumUnmarshallers {
  final def enumUnmarshaller[E <: Enum](using e: EnumLike[E]): FromStringUnmarshaller[E] = Unmarshaller { name =>
    e.values.find(_.toString().toLowerCase() == name.toLowerCase()) match {
      case Some(enumEntry) => IO.pure(enumEntry)
      case None =>
        IO.raiseError(new IllegalArgumentException(s"""Invalid value '$name'. Expected one of: ${e.values.mkString(", ")}"""))
    }
  }

  implicit def kebsEnumUnmarshaller[E <: Enum](using e: EnumLike[E]): FromStringUnmarshaller[E] =
    enumUnmarshaller
}

trait ValueEnumUnmarshallers extends EnumUnmarshallers {
  final def valueEnumUnmarshaller[V, E <: ValueEnumLikeEntry[V]](using `enum`: ValueEnumLike[V, E], cls: ClassTag[V]): Unmarshaller[V, E] =
    Unmarshaller {
      v =>
        `enum`.values.find(e => e.value == v && e.value.getClass == v.getClass) match {
          case Some(enumEntry) =>
            IO.pure(enumEntry)
          case _ =>
            `enum`.values.find(e => e.value == v) match {
              case Some(enumEntry) =>
                IO.raiseError(new IllegalArgumentException(s"""Invalid value '$v'"""))
              case None =>
                IO.raiseError(new IllegalArgumentException(s"""Invalid value '$v'. Expected one of: ${`enum`.values.map(_.value).mkString(", ")}"""))
            }
        }
    }

  implicit def kebsValueEnumUnmarshaller[V, E <: ValueEnumLikeEntry[V]](using `enum`: ValueEnumLike[V, E], cls: ClassTag[V]): Unmarshaller[V, E] =
    valueEnumUnmarshaller

  implicit def kebsIntValueEnumFromStringUnmarshaller[E <: ValueEnumLikeEntry[Int]](using ev: ValueEnumLike[Int, E]): FromStringUnmarshaller[E] =
    intFromStringUnmarshaller andThen valueEnumUnmarshaller

  implicit def kebsLongValueEnumFromStringUnmarshaller[E <: ValueEnumLikeEntry[Long]](using ev: ValueEnumLike[Long, E]): FromStringUnmarshaller[E] =
    longFromStringUnmarshaller andThen valueEnumUnmarshaller

  implicit def kebsShortValueEnumFromStringUnmarshaller[E <: ValueEnumLikeEntry[Short]](using ev: ValueEnumLike[Short, E]): FromStringUnmarshaller[E] =
    shortFromStringUnmarshaller andThen valueEnumUnmarshaller

  implicit def kebsByteValueEnumFromStringUnmarshaller[E <: ValueEnumLikeEntry[Byte]](using ev: ValueEnumLike[Byte, E]): FromStringUnmarshaller[E] =
    byteFromStringUnmarshaller andThen valueEnumUnmarshaller
}

trait KebsEnumUnmarshallers extends ValueEnumUnmarshallers {}
