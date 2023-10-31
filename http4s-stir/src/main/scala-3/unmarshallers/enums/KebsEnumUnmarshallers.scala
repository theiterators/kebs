package pl.iterators.kebs.unmarshallers.enums

import pl.iterators.stir.unmarshalling.PredefinedFromStringUnmarshallers._
import pl.iterators.stir.unmarshalling.{FromStringUnmarshaller, Unmarshaller}
import cats.effect.IO
import scala.reflect.Enum
import scala.reflect.ClassTag
import pl.iterators.kebs.enums.{ValueEnumLike, EnumLike}

import reflect.Selectable.reflectiveSelectable

trait EnumUnmarshallers {
  final def enumUnmarshaller[E <: Enum](using e: EnumLike[E]): FromStringUnmarshaller[E] = Unmarshaller { name =>
    e.values.find(_.toString().toLowerCase() == name.toLowerCase()) match {
      case Some(enumEntry) => IO.pure(enumEntry)
      case None =>
        IO.raiseError(new IllegalArgumentException(s"""Invalid value '$name'. Expected one of: ${e.values.mkString(", ")}"""))
    }
  }

  given kebsEnumUnmarshaller[E <: Enum](using e: EnumLike[E]): FromStringUnmarshaller[E] =
    enumUnmarshaller
}

trait ValueEnumUnmarshallers extends EnumUnmarshallers {
  final def valueEnumUnmarshaller[V, E <: { def value: V }](using `enum`: ValueEnumLike[V, E], cls: ClassTag[V]): Unmarshaller[V, E] = Unmarshaller { v =>
    `enum`.values.find(e => e.value == v) match {
      case Some(enumEntry) => IO.pure(enumEntry)
      case None =>
        IO.raiseError(new IllegalArgumentException(s"""Invalid value '$v'. Expected one of: ${`enum`.values.map(_.value).mkString(", ")}"""))
    }
  }

  given kebsValueEnumUnmarshaller[V, E <: { def value: V }](using `enum`: ValueEnumLike[V, E], cls: ClassTag[V]): Unmarshaller[V, E] =
    valueEnumUnmarshaller

  given kebsIntValueEnumFromStringUnmarshaller[E <: { def value: Int }](using ev: ValueEnumLike[Int, E]): FromStringUnmarshaller[E] =
    intFromStringUnmarshaller andThen valueEnumUnmarshaller
  given kebsLongValueEnumFromStringUnmarshaller[E <: { def value: Long }](using ev: ValueEnumLike[Long, E]): FromStringUnmarshaller[E] =
    longFromStringUnmarshaller andThen valueEnumUnmarshaller
  given kebsShortValueEnumFromStringUnmarshaller[E <: { def value: Short }](
      using ev: ValueEnumLike[Short, E]): FromStringUnmarshaller[E] =
    shortFromStringUnmarshaller andThen valueEnumUnmarshaller
  given kebsByteValueEnumFromStringUnmarshaller[E <: { def value: Byte }](using ev: ValueEnumLike[Byte, E]): FromStringUnmarshaller[E] =
    byteFromStringUnmarshaller andThen valueEnumUnmarshaller
}

trait KebsEnumUnmarshallers extends ValueEnumUnmarshallers {}
