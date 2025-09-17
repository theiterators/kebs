package pl.iterators.kebs.http4sstir.unmarshallers

import pl.iterators.kebs.core.instances.InstanceConverter
import pl.iterators.kebs.core.macros.ValueClassLike
import pl.iterators.stir.unmarshalling.{FromStringUnmarshaller, Unmarshaller}
import pl.iterators.stir.unmarshalling.PredefinedFromStringUnmarshallers._
import cats.effect.IO
import pl.iterators.kebs.core.enums.{EnumLike, ValueEnumLike, ValueEnumLikeEntry}

trait KebsHttp4sStirEnumUnmarshallers {
  private final def enumUnmarshaller[E](`enum`: EnumLike[E]): FromStringUnmarshaller[E] = Unmarshaller { name =>
    `enum`.withNameInsensitiveOption(name) match {
      case Some(enumEntry) => IO.pure(enumEntry)
      case None            =>
        IO.raiseError(new IllegalArgumentException(s"""Invalid value '$name'. Expected one of: ${`enum`.getNamesToValuesMap.keysIterator
            .mkString(", ")}"""))
    }
  }

  implicit def kebsEnumUnmarshaller[E](implicit ev: EnumLike[E]): FromStringUnmarshaller[E] =
    enumUnmarshaller(ev)
}

private[kebs] trait LowerPriorityKebsHttp4sStirValueEnumUnmarshallers {
  final def valueEnumUnmarshaller[V, E <: ValueEnumLikeEntry[V]](`enum`: ValueEnumLike[V, E]): Unmarshaller[V, E] = Unmarshaller { v =>
    `enum`.values.find(e => e.value == v) match {
      case Some(enumEntry) => IO.pure(enumEntry)
      case None            =>
        IO.raiseError(
          new IllegalArgumentException(s"""Invalid value '$v'. Expected one of: ${`enum`.getValuesToEntriesMap.keysIterator
              .mkString(", ")}""")
        )
    }
  }

  implicit def kebsValueEnumFromStringUnmarshaller[E <: ValueEnumLikeEntry[String]](implicit
      ev: ValueEnumLike[String, E]
  ): FromStringUnmarshaller[E] = valueEnumUnmarshaller(ev)

  implicit def kebsIntValueEnumFromStringUnmarshaller[E <: ValueEnumLikeEntry[Int]](implicit
      ev: ValueEnumLike[Int, E]
  ): FromStringUnmarshaller[E] =
    intFromStringUnmarshaller andThen valueEnumUnmarshaller(ev)
  implicit def kebsLongValueEnumFromStringUnmarshaller[E <: ValueEnumLikeEntry[Long]](implicit
      ev: ValueEnumLike[Long, E]
  ): FromStringUnmarshaller[E] =
    longFromStringUnmarshaller andThen valueEnumUnmarshaller(ev)
  implicit def kebsShortValueEnumFromStringUnmarshaller[E <: ValueEnumLikeEntry[Short]](implicit
      ev: ValueEnumLike[Short, E]
  ): FromStringUnmarshaller[E] =
    shortFromStringUnmarshaller andThen valueEnumUnmarshaller(ev)
  implicit def kebsByteValueEnumFromStringUnmarshaller[E <: ValueEnumLikeEntry[Byte]](implicit
      ev: ValueEnumLike[Byte, E]
  ): FromStringUnmarshaller[E] =
    byteFromStringUnmarshaller andThen valueEnumUnmarshaller(ev)
}

trait KebsHttp4sStirValueEnumUnmarshallers extends LowerPriorityKebsHttp4sStirValueEnumUnmarshallers {
  implicit def kebsValueEnumUnmarshaller[V, E <: ValueEnumLikeEntry[V]](implicit ev: ValueEnumLike[V, E]): Unmarshaller[V, E] =
    valueEnumUnmarshaller(ev)
}

trait KebsHttp4sStirUnmarshallers
    extends LowPriorityKebsUnmarshallers
    with KebsHttp4sStirEnumUnmarshallers
    with KebsHttp4sStirValueEnumUnmarshallers {
  @inline
  implicit def kebsFromStringUnmarshaller[A, B](implicit
      rep: ValueClassLike[B, A],
      fsu: FromStringUnmarshaller[A]
  ): FromStringUnmarshaller[B] =
    fsu andThen kebsUnmarshaller(rep)

  @inline
  implicit def kebsInstancesFromStringUnmarshaller[A, B](implicit
      ico: InstanceConverter[B, A],
      fsu: FromStringUnmarshaller[A]
  ): FromStringUnmarshaller[B] =
    fsu andThen kebsInstancesUnmarshaller(ico)

  // this is to make both 2.13.x and 3.x compilers happy
  override implicit def kebsValueEnumUnmarshaller[V, E <: ValueEnumLikeEntry[V]](implicit ev: ValueEnumLike[V, E]): Unmarshaller[V, E] =
    valueEnumUnmarshaller(ev)
}

private[kebs] trait LowPriorityKebsUnmarshallers {
  implicit def kebsInstancesUnmarshaller[A, B](implicit ico: InstanceConverter[B, A]): Unmarshaller[A, B] =
    Unmarshaller.strict[A, B](ico.decode)

  implicit def kebsUnmarshaller[A, B](implicit rep: ValueClassLike[B, A]): Unmarshaller[A, B] =
    Unmarshaller.strict[A, B](rep.apply)
}
