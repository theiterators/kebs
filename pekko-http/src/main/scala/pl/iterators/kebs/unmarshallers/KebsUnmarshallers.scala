package pl.iterators.kebs.unmarshallers

import org.apache.pekko.http.scaladsl.unmarshalling.{FromStringUnmarshaller, Unmarshaller}
import pl.iterators.kebs.instances.InstanceConverter
import pl.iterators.kebs.macros.CaseClass1Rep

trait KebsUnmarshallers extends LowPriorityKebsUnmarshallers {
  @inline
  implicit def kebsFromStringUnmarshaller[A, B](implicit rep: CaseClass1Rep[B, A],
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

  implicit def kebsUnmarshaller[A, B](implicit rep: CaseClass1Rep[B, A]): Unmarshaller[A, B] =
    Unmarshaller.strict[A, B](rep.apply)
}