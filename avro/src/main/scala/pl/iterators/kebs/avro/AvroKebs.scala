package pl.iterators.kebs.avro

import com.sksamuel.avro4s._
import org.apache.avro.Schema
import pl.iterators.kebs.macros.CaseClass1Rep

trait AvroKebs {
  implicit def valueTypeToSchema[CC <: AnyVal, A](implicit rep: CaseClass1Rep[CC, A], subschema: ToSchema[A]): ToSchema[CC] =
    new ToSchema[CC] {
      override protected val schema = subschema()
    }
  implicit def valueTypeToValue[CC <: AnyVal, A](implicit rep: CaseClass1Rep[CC, A], delegate: ToValue[A]): ToValue[CC] =
    new ToValue[CC] {
      override def apply(value: CC) = delegate(rep.unapply(value))
    }
  implicit def valueTypeFromValue[CC <: AnyVal, B](implicit rep: CaseClass1Rep[CC, B], delegate: FromValue[B]): FromValue[CC] =
    new FromValue[CC] {
      override def apply(value: Any, field: Schema.Field) = rep.apply(delegate(value, field))
    }

}
