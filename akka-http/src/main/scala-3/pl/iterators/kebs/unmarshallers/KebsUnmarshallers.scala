package pl.iterators.kebs.unmarshallers

import akka.http.scaladsl.unmarshalling.{FromStringUnmarshaller, Unmarshaller}
import pl.iterators.kebs.instances.InstanceConverter
import pl.iterators.kebs.macros.CaseClass1Rep

trait KebsUnmarshallers {
  implicit def kebsUnmarshaller[A, B](implicit rep: CaseClass1Rep[B, A]): Unmarshaller[A, B] =
    Unmarshaller.strict[A, B](rep.apply)

  // inline implicit def kebsFromStringUnmarshaller[A, B](implicit rep: CaseClass1Rep[B, A],
  //                                               fsu: FromStringUnmarshaller[A]): FromStringUnmarshaller[B] =
  //   fsu andThen kebsUnmarshaller(rep)

  implicit def kebsInstancesUnmarshaller[A, B](implicit ico: InstanceConverter[B, A]): Unmarshaller[A, B] =
    Unmarshaller.strict[A, B](ico.decode)
  
  inline implicit def kebsInstancesFromStringUnmarshaller[A, B](implicit ico: InstanceConverter[B, A],
                                                         fsu: FromStringUnmarshaller[A]): FromStringUnmarshaller[B] =
    fsu andThen kebsInstancesUnmarshaller(ico)

}