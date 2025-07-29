package pl.iterators.kebs.baklava.schema

import pl.iterators.baklava.{Schema, SchemaType}
import pl.iterators.kebs.core.instances.InstanceConverter
import pl.iterators.kebs.core.macros.ValueClassLike

import scala.annotation.unused
import scala.reflect.ClassTag

trait KebsBaklavaSchema {
  implicit def valueClassLikeSchema[T, A](implicit
      @unused valueClassLike: ValueClassLike[T, A],
      schema: Schema[A],
      cls: ClassTag[T]
  ): Schema[T] = {
    new Schema[T] {
      val className: String                  = cls.runtimeClass.getName // TODO: this won't capture opaque type's name
      val `type`: SchemaType                 = schema.`type`
      val format: Option[String]             = schema.format
      val properties: Map[String, Schema[?]] = schema.properties
      val items: Option[Schema[?]]           = schema.items
      val `enum`: Option[Set[String]]        = schema.`enum`
      val required: Boolean                  = schema.required
      val additionalProperties: Boolean      = schema.additionalProperties
      val default: Option[T]                 = None
      val description: Option[String]        = schema.description
    }
  }

  implicit def instanceConverter[T, A](implicit
      @unused instanceConverter: InstanceConverter[T, A],
      schema: Schema[A],
      cls: ClassTag[T]
  ): Schema[T] = {
    new Schema[T] {
      val className: String  = cls.runtimeClass.getName
      val `type`: SchemaType = schema.`type`
      val format: Option[String] = cls.runtimeClass.getName match {
        case "java.time.LocalDate"     => Some("date")
        case "java.time.Instant"       => Some("date-time")
        case "java.time.ZonedDateTime" => Some("date-time")
        case "java.net.URI"            => Some("uri")
        case _                         => schema.format
      }
      val properties: Map[String, Schema[?]] = schema.properties
      val items: Option[Schema[?]]           = schema.items
      val `enum`: Option[Set[String]]        = schema.`enum`
      val required: Boolean                  = schema.required
      val additionalProperties: Boolean      = schema.additionalProperties
      val default: Option[T]                 = None
      val description: Option[String]        = schema.description
    }
  }
}
