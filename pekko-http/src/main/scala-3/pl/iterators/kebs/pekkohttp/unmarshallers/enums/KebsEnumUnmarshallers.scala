package pl.iterators.kebs.pekkohttp.unmarshallers.enums

import org.apache.pekko.http.scaladsl.unmarshalling.PredefinedFromStringUnmarshallers.*
import org.apache.pekko.http.scaladsl.unmarshalling.{FromStringUnmarshaller, Unmarshaller}
import org.apache.pekko.http.scaladsl.util.FastFuture

import scala.reflect.ClassTag

import pl.iterators.kebs.core.enums.{EnumLike, ValueEnumLike, ValueEnumLikeEntry}

trait EnumUnmarshallers {

  final def enumUnmarshaller[E](using e: EnumLike[E]): FromStringUnmarshaller[E] =
    org.apache.pekko.http.scaladsl.unmarshalling.Unmarshaller { _ => name =>
      e.values.find(_.toString().toLowerCase() == name.toLowerCase()) match {
        case Some(enumEntry) => FastFuture.successful(enumEntry)
        case None =>
          FastFuture.failed(new IllegalArgumentException(s"""Invalid value '$name'. Expected one of: ${e.values.mkString(", ")}"""))
      }
    }

  implicit def kebsEnumUnmarshaller[E](using e: EnumLike[E]): FromStringUnmarshaller[E] =
    enumUnmarshaller
}

trait ValueEnumUnmarshallers extends EnumUnmarshallers {
  final def valueEnumUnmarshaller[V, E <: ValueEnumLikeEntry[V]](using `enum`: ValueEnumLike[V, E], cls: ClassTag[V]): Unmarshaller[V, E] =
    Unmarshaller { _ => v =>
      `enum`.values.find(e => e.value == v && e.value.getClass == v.getClass) match {
        case Some(enumEntry) =>
          FastFuture.successful(enumEntry)
        case _ =>
          `enum`.values.find(e => e.value == v) match {
            case Some(enumEntry) =>
              FastFuture.failed(new IllegalArgumentException(s"""Invalid value '$v'"""))
            case None =>
              FastFuture.failed(
                new IllegalArgumentException(s"""Invalid value '$v'. Expected one of: ${`enum`.values.map(_.value).mkString(", ")}""")
              )
          }
      }
    }

  implicit def kebsValueEnumUnmarshaller[V, E <: ValueEnumLikeEntry[V]](using
      `enum`: ValueEnumLike[V, E],
      cls: ClassTag[V]
  ): Unmarshaller[V, E] =
    valueEnumUnmarshaller

  implicit def kebsIntValueEnumFromStringUnmarshaller[E <: ValueEnumLikeEntry[Int]](using
      ev: ValueEnumLike[Int, E]
  ): FromStringUnmarshaller[E] =
    intFromStringUnmarshaller andThen valueEnumUnmarshaller

  implicit def kebsLongValueEnumFromStringUnmarshaller[E <: ValueEnumLikeEntry[Long]](using
      ev: ValueEnumLike[Long, E]
  ): FromStringUnmarshaller[E] =
    longFromStringUnmarshaller andThen valueEnumUnmarshaller

  implicit def kebsShortValueEnumFromStringUnmarshaller[E <: ValueEnumLikeEntry[Short]](using
      ev: ValueEnumLike[Short, E]
  ): FromStringUnmarshaller[E] =
    shortFromStringUnmarshaller andThen valueEnumUnmarshaller

  implicit def kebsByteValueEnumFromStringUnmarshaller[E <: ValueEnumLikeEntry[Byte]](using
      ev: ValueEnumLike[Byte, E]
  ): FromStringUnmarshaller[E] =
    byteFromStringUnmarshaller andThen valueEnumUnmarshaller
}

trait KebsEnumUnmarshallers extends ValueEnumUnmarshallers {}
