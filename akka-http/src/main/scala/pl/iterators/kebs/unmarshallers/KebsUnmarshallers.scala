package pl.iterators.kebs.unmarshallers

import akka.http.scaladsl.unmarshalling.{FromStringUnmarshaller, Unmarshaller}
import pl.iterators.kebs.unmarshallers.KebsUnmarshallers.InvariantDummy

trait KebsFromStringUnmarshallers {
  @inline
  def kebsFromStringUnmarshaller[A, A1 <: A, B](um: Unmarshaller[A, B])(
      implicit fsu: FromStringUnmarshaller[A1]): FromStringUnmarshaller[B] =
    fsu andThen um
}

trait KebsUnmarshallers extends KebsFromStringUnmarshallers {
  import macros.KebsUnmarshallersMacros
  implicit def kebsUnmarshaller[A: InvariantDummy, B <: Product]: Unmarshaller[A, B] =
    macro KebsUnmarshallersMacros.materializeUnmarshaller[A, B]
}

object KebsUnmarshallers {
  trait InvariantDummy[T]
  object InvariantDummy {
    implicit def materialize[T]: InvariantDummy[T] = null.asInstanceOf[InvariantDummy[T]]
  }
}
