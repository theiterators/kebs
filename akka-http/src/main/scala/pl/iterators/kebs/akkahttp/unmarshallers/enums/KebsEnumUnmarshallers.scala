package pl.iterators.kebs.akkahttp.unmarshallers.enums

import akka.http.scaladsl.unmarshalling.PredefinedFromStringUnmarshallers._
import akka.http.scaladsl.unmarshalling.{FromStringUnmarshaller, Unmarshaller}
import akka.http.scaladsl.util.FastFuture
import pl.iterators.kebs.core.enums.{EnumLike, ValueEnumLike, ValueEnumLikeEntry}

trait EnumUnmarshallers {
  final def enumUnmarshaller[E](`enum`: EnumLike[E]): FromStringUnmarshaller[E] = Unmarshaller { _ =>name =>
    `enum`.withNameInsensitiveOption(name) match {
      case Some(enumEntry) => FastFuture.successful(enumEntry)
      case None =>
        FastFuture.failed(new IllegalArgumentException(s"""Invalid value '$name'. Expected one of: ${`enum`.getNamesToValuesMap.keysIterator
          .mkString(", ")}"""))
    }
  }

  implicit def kebsEnumUnmarshaller[E](implicit ev: EnumLike[E]): FromStringUnmarshaller[E] =
    enumUnmarshaller(ev)
}

trait ValueEnumUnmarshallers {
  final def valueEnumUnmarshaller[V, E <: ValueEnumLikeEntry[V]](`enum`: ValueEnumLike[V, E]): Unmarshaller[V, E] = Unmarshaller { _ =>v =>
    `enum`.withValueOption(v) match {
      case Some(enumEntry) => FastFuture.successful(enumEntry)
      case None =>
        FastFuture.failed(new IllegalArgumentException(s"""Invalid value '$v'. Expected one of: ${`enum`.getValuesToEntriesMap.keysIterator
          .mkString(", ")}"""))
    }
  }

  implicit def kebsValueEnumUnmarshaller[V, E <: ValueEnumLikeEntry[V]](implicit ev: ValueEnumLike[V, E]): Unmarshaller[V, E] =
    valueEnumUnmarshaller(ev)

  implicit def kebsIntValueEnumFromStringUnmarshaller[E <: ValueEnumLikeEntry[Int]](implicit ev: ValueEnumLike[Int, E]): FromStringUnmarshaller[E] =
    intFromStringUnmarshaller andThen valueEnumUnmarshaller(ev)
  implicit def kebsLongValueEnumFromStringUnmarshaller[E <: ValueEnumLikeEntry[Long]](implicit ev: ValueEnumLike[Long, E]): FromStringUnmarshaller[E] =
    longFromStringUnmarshaller andThen valueEnumUnmarshaller(ev)
  implicit def kebsShortValueEnumFromStringUnmarshaller[E <: ValueEnumLikeEntry[Short]](
      implicit ev: ValueEnumLike[Short, E]): FromStringUnmarshaller[E] =
    shortFromStringUnmarshaller andThen valueEnumUnmarshaller(ev)
  implicit def kebsByteValueEnumFromStringUnmarshaller[E <: ValueEnumLikeEntry[Byte]](implicit ev: ValueEnumLike[Byte, E]): FromStringUnmarshaller[E] =
    byteFromStringUnmarshaller andThen valueEnumUnmarshaller(ev)
}

trait KebsEnumUnmarshallers extends EnumUnmarshallers with ValueEnumUnmarshallers {}
