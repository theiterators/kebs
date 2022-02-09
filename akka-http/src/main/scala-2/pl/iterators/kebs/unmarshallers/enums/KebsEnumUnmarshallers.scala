package pl.iterators.kebs.unmarshallers.enums

import akka.http.scaladsl.unmarshalling.PredefinedFromStringUnmarshallers._
import akka.http.scaladsl.unmarshalling.{FromStringUnmarshaller, Unmarshaller}
import akka.http.scaladsl.util.FastFuture
import enumeratum.values._
import enumeratum.{Enum, EnumEntry}
import pl.iterators.kebs.macros.enums.{EnumOf, ValueEnumOf}

trait EnumUnmarshallers {
  final def enumUnmarshaller[E <: EnumEntry](`enum`: Enum[E]): FromStringUnmarshaller[E] = Unmarshaller { _ =>name =>
    `enum`.withNameInsensitiveOption(name) match {
      case Some(enumEntry) => FastFuture.successful(enumEntry)
      case None =>
        FastFuture.failed(new IllegalArgumentException(s"""Invalid value '$name'. Expected one of: ${`enum`.namesToValuesMap.keysIterator
          .mkString(", ")}"""))
    }
  }

  implicit def kebsEnumUnmarshaller[E <: EnumEntry](implicit ev: EnumOf[E]): FromStringUnmarshaller[E] =
    enumUnmarshaller(ev.`enum`)
}

trait ValueEnumUnmarshallers {
  final def valueEnumUnmarshaller[V, E <: ValueEnumEntry[V]](`enum`: ValueEnum[V, E]): Unmarshaller[V, E] = Unmarshaller { _ =>v =>
    `enum`.withValueOpt(v) match {
      case Some(enumEntry) => FastFuture.successful(enumEntry)
      case None =>
        FastFuture.failed(new IllegalArgumentException(s"""Invalid value '$v'. Expected one of: ${`enum`.valuesToEntriesMap.keysIterator
          .mkString(", ")}"""))
    }
  }

  implicit def kebsValueEnumUnmarshaller[V, E <: ValueEnumEntry[V]](implicit ev: ValueEnumOf[V, E]): Unmarshaller[V, E] =
    valueEnumUnmarshaller(ev.valueEnum)

  implicit def kebsIntValueEnumFromStringUnmarshaller[E <: IntEnumEntry](implicit ev: ValueEnumOf[Int, E]): FromStringUnmarshaller[E] =
    intFromStringUnmarshaller andThen valueEnumUnmarshaller(ev.valueEnum)
  implicit def kebsLongValueEnumFromStringUnmarshaller[E <: LongEnumEntry](implicit ev: ValueEnumOf[Long, E]): FromStringUnmarshaller[E] =
    longFromStringUnmarshaller andThen valueEnumUnmarshaller(ev.valueEnum)
  implicit def kebsShortValueEnumFromStringUnmarshaller[E <: ShortEnumEntry](
      implicit ev: ValueEnumOf[Short, E]): FromStringUnmarshaller[E] =
    shortFromStringUnmarshaller andThen valueEnumUnmarshaller(ev.valueEnum)
  implicit def kebsByteValueEnumFromStringUnmarshaller[E <: ByteEnumEntry](implicit ev: ValueEnumOf[Byte, E]): FromStringUnmarshaller[E] =
    byteFromStringUnmarshaller andThen valueEnumUnmarshaller(ev.valueEnum)
}

trait KebsEnumUnmarshallers extends EnumUnmarshallers with ValueEnumUnmarshallers {}
