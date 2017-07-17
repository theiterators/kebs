package pl.iterators.kebs.avro

import com.sksamuel.avro4s._
import org.apache.avro.Schema

trait AvroKebs {
  import macros.AvroKebsMacros
  implicit def valueTypeToSchema[CC <: AnyVal with Product]: ToSchema[CC] = macro AvroKebsMacros.materializeToSchema[CC]
  implicit def valueTypeToValue[CC <: AnyVal with Product]: ToValue[CC] = macro AvroKebsMacros.materializeToValue[CC]
  implicit def valueTypeFromValue[CC <: AnyVal with Product]: FromValue[CC] = macro AvroKebsMacros.materializeFromValue[CC]

  @inline
  final def wrapToSchema[A](subschema: ToSchema[_]): ToSchema[A] = new ToSchema[A] {
    override protected val schema = subschema()
  }

  @inline
  final def wrapToValue[A, B](getValue: A => B, delegate: ToValue[B]): ToValue[A] = new ToValue[A] {
    override def apply(value: A) = delegate(getValue(value))
  }

  @inline
  final def wrapFromValue[A, B](construct: B => A, delegate: FromValue[B]): FromValue[A] = new FromValue[A] {
    override def apply(value: Any, field: Schema.Field) = construct(delegate(value, field))
  }
}
