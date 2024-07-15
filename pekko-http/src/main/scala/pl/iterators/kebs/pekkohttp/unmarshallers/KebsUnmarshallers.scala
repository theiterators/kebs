package pl.iterators.kebs.pekkohttp.unmarshallers

import org.apache.pekko.http.scaladsl.unmarshalling.{FromStringUnmarshaller, Unmarshaller}
import pl.iterators.kebs.core.instances.InstanceConverter
import pl.iterators.kebs.core.macros.ValueClassLike

trait KebsUnmarshallers extends LowPriorityKebsUnmarshallers {
  @inline
  implicit def kebsFromStringUnmarshaller[A, B](implicit rep: ValueClassLike[B, A],
                                                fsu: FromStringUnmarshaller[A]): FromStringUnmarshaller[B] =
    fsu andThen kebsUnmarshaller(rep)


  @inline
  implicit def kebsInstancesFromStringUnmarshaller[A, B](implicit ico: InstanceConverter[B, A],
                                                         fsu: FromStringUnmarshaller[A]): FromStringUnmarshaller[B] =
    fsu andThen kebsInstancesUnmarshaller(ico)

}

trait LowPriorityKebsUnmarshallers {
  implicit def kebsInstancesUnmarshaller[A, B](implicit ico: InstanceConverter[B, A]): Unmarshaller[A, B] =
    Unmarshaller.strict[A, B](ico.decode)

  implicit def kebsUnmarshaller[A, B](implicit rep: ValueClassLike[B, A]): Unmarshaller[A, B] =
    Unmarshaller.strict[A, B](rep.apply)
}