package pl.iterators.kebs.akkahttp.unmarshallers

import akka.http.scaladsl.unmarshalling.{FromStringUnmarshaller, Unmarshaller}
import pl.iterators.kebs.core.instances.InstanceConverter
import pl.iterators.kebs.core.macros.ValueClassLike

trait KebsUnmarshallers {
  implicit def kebsUnmarshaller[A, B](implicit rep: ValueClassLike[B, A]): Unmarshaller[A, B] =
    Unmarshaller.strict[A, B](rep.apply)
  @inline
  implicit def kebsFromStringUnmarshaller[A, B](implicit
      rep: ValueClassLike[B, A],
      fsu: FromStringUnmarshaller[A]
  ): FromStringUnmarshaller[B] =
    fsu andThen kebsUnmarshaller(rep)

  implicit def kebsInstancesUnmarshaller[A, B](implicit ico: InstanceConverter[B, A]): Unmarshaller[A, B] =
    Unmarshaller.strict[A, B](ico.decode)
  @inline
  implicit def kebsInstancesFromStringUnmarshaller[A, B](implicit
      ico: InstanceConverter[B, A],
      fsu: FromStringUnmarshaller[A]
  ): FromStringUnmarshaller[B] =
    fsu andThen kebsInstancesUnmarshaller(ico)

}
