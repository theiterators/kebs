package pl.iterators.kebs.jsonschema

import pl.iterators.kebs.core.macros.ValueClassLike

import scala.annotation.unused

@deprecated("kebs-jsonschema is deprecated: scala-jsonschema is no longer maintained", "3.0.0")
trait KebsJsonSchema {
  import macros.KebsJsonSchemaMacros
  implicit val jswUnit: JsonSchemaWrapper[Unit] = JsonSchemaWrapper[Unit](null)

  implicit def valueClassLikeJsonSchemaPredef[T, A](implicit
      @unused rep: ValueClassLike[T, A],
      schema: json.schema.Predef[A]
  ): json.schema.Predef[T] =
    schema.asInstanceOf[json.schema.Predef[T]]
  implicit def genericJsonSchemaWrapper[T]: JsonSchemaWrapper[T] = macro KebsJsonSchemaMacros.materializeSchema[T]
}
