package pl.iterators.kebs.unmarshallers

import org.apache.pekko.http.scaladsl.unmarshalling.{FromStringUnmarshaller, Unmarshaller}
import pl.iterators.kebs.instances.InstanceConverter
import pl.iterators.kebs.macros.{ValueClassLike, FlatCaseClass1}

trait KebsUnmarshallers extends LowPriorityKebsUnmarshallers with FlatCaseClass1 {
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