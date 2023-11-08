package pl.iterators.kebs.unmarshallers.enums

import org.apache.pekko.http.scaladsl.unmarshalling.PredefinedFromStringUnmarshallers.*
import org.apache.pekko.http.scaladsl.unmarshalling.{FromStringUnmarshaller, Unmarshaller}
import org.apache.pekko.http.scaladsl.util.FastFuture
import pl.iterators.kebs.enums.{EnumLike, ValueEnumLike}

import scala.reflect.ClassTag
import reflect.Selectable._

trait EnumUnmarshallers {
  final def enumUnmarshaller[E](using e: EnumLike[E]): FromStringUnmarshaller[E] = org.apache.pekko.http.scaladsl.unmarshalling.Unmarshaller { _ => name =>
    e.values.find(_.toString().toLowerCase() == name.toLowerCase()) match {
      case Some(enumEntry) => FastFuture.successful(enumEntry)
      case None =>
        FastFuture.failed(new IllegalArgumentException(s"""Invalid value '$name'. Expected one of: ${e.values.mkString(", ")}"""))
    }
  }

  given kebsEnumUnmarshaller[E](using e: EnumLike[E]): FromStringUnmarshaller[E] =
    enumUnmarshaller
}

trait ValueEnumUnmarshallers extends EnumUnmarshallers {
  final def valueEnumUnmarshaller[V, E <: {def value: V}](using `enum`: ValueEnumLike[V, E]): Unmarshaller[V, E] =
    Unmarshaller { _ =>
      v =>
        `enum`.values.find(e => e.value == v && e.value.getClass == v.getClass) match {
          case Some(enumEntry) =>
            FastFuture.successful(enumEntry)
          case _ =>
            `enum`.values.find(e => e.value == v) match {
              case Some(_) =>
                FastFuture.failed(new IllegalArgumentException(s"""Invalid value '$v'"""))
              case None =>
                FastFuture.failed(new IllegalArgumentException(s"""Invalid value '$v'. Expected one of: ${`enum`.values.map(_.value).mkString(", ")}"""))
            }
        }
  }
}

trait LowPriorityImplicits extends ValueEnumUnmarshallers {
  given kebsValueEnumUnmarshaller[V, E <: {def value: V}](using `enum`: ValueEnumLike[V, E], cls: ClassTag[V]): Unmarshaller[V, E] =
    valueEnumUnmarshaller
}

trait HighPriorityImplicits extends LowPriorityImplicits {
  given kebsIntValueEnumFromStringUnmarshaller[E <: {def value: Int}](using ev: ValueEnumLike[Int, E]): FromStringUnmarshaller[E] =
    intFromStringUnmarshaller andThen valueEnumUnmarshaller

  given kebsLongValueEnumFromStringUnmarshaller[E <: {def value: Long}](using ev: ValueEnumLike[Long, E]): FromStringUnmarshaller[E] =
    longFromStringUnmarshaller andThen valueEnumUnmarshaller

  given kebsShortValueEnumFromStringUnmarshaller[E <: {def value: Short}](using ev: ValueEnumLike[Short, E]): FromStringUnmarshaller[E] =
    shortFromStringUnmarshaller andThen valueEnumUnmarshaller

  given kebsByteValueEnumFromStringUnmarshaller[E <: {def value: Byte}](using ev: ValueEnumLike[Byte, E]): FromStringUnmarshaller[E] =
    byteFromStringUnmarshaller andThen valueEnumUnmarshaller
}

trait KebsEnumUnmarshallers extends HighPriorityImplicits with LowPriorityImplicits {}
