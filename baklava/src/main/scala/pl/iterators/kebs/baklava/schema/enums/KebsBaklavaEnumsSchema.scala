package pl.iterators.kebs.baklava.schema.enums

import pl.iterators.baklava.{Schema, SchemaType}
import pl.iterators.kebs.core.enums.{EnumLike, ValueEnumLike, ValueEnumLikeEntry}

import scala.reflect.ClassTag

trait KebsBaklavaEnumsSchema {
  implicit def enumLikeSchema[T](implicit enumLike: EnumLike[T], cls: ClassTag[T]): Schema[T] =
    new Schema[T] {
      val className: String                  = cls.runtimeClass.getName
      val `type`: SchemaType                 = SchemaType.StringType
      val format: Option[String]             = None
      val properties: Map[String, Schema[?]] = Map.empty
      val items: Option[Schema[?]]           = None
      val `enum`: Option[Set[String]]        = Some(enumLike.values.map(_.toString).toSet)
      val required: Boolean                  = true
      val additionalProperties: Boolean      = false
      val default: Option[T]                 = None
      val description: Option[String]        = None
    }

  trait KebsBaklavaEnumsUppercaseSchema {
    implicit def enumLikeSchema[T](implicit enumLike: EnumLike[T], cls: ClassTag[T]): Schema[T] =
      new Schema[T] {
        val className: String                  = cls.runtimeClass.getName
        val `type`: SchemaType                 = SchemaType.StringType
        val format: Option[String]             = None
        val properties: Map[String, Schema[?]] = Map.empty
        val items: Option[Schema[?]]           = None
        val `enum`: Option[Set[String]]        = Some(enumLike.values.map(_.toString.toUpperCase).toSet)
        val required: Boolean                  = true
        val additionalProperties: Boolean      = false
        val default: Option[T]                 = None
        val description: Option[String]        = None
      }
  }

  trait KebsBaklavaEnumsLowercaseSchema {
    implicit def enumLikeSchema[T](implicit enumLike: EnumLike[T], cls: ClassTag[T]): Schema[T] =
      new Schema[T] {
        val className: String                  = cls.runtimeClass.getName
        val `type`: SchemaType                 = SchemaType.StringType
        val format: Option[String]             = None
        val properties: Map[String, Schema[?]] = Map.empty
        val items: Option[Schema[?]]           = None
        val `enum`: Option[Set[String]]        = Some(enumLike.values.map(_.toString.toLowerCase).toSet)
        val required: Boolean                  = true
        val additionalProperties: Boolean      = false
        val default: Option[T]                 = None
        val description: Option[String]        = None
      }
  }
}

trait KebsBaklavaValueEnumsSchema {
  implicit def valueEnumLikeSchema[T, V <: ValueEnumLikeEntry[T]](implicit
      valueEnumLike: ValueEnumLike[T, V],
      schema: Schema[V],
      cls: ClassTag[T]
  ): Schema[T] = {
    new Schema[T] {
      val className: String                  = cls.runtimeClass.getName
      val `type`: SchemaType                 = schema.`type`
      val format: Option[String]             = schema.format
      val properties: Map[String, Schema[?]] = schema.properties
      val items: Option[Schema[?]]           = schema.items
      val `enum`: Option[Set[String]]        = Some(valueEnumLike.values.map(_.toString).toSet)
      val required: Boolean                  = schema.required
      val additionalProperties: Boolean      = schema.additionalProperties
      val default: Option[T]                 = None
      val description: Option[String]        = schema.description
    }
  }
}
