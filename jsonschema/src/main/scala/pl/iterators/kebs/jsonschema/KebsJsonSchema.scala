package pl.iterators.kebs.jsonschema

import pl.iterators.kebs.core.macros.ValueClassLike

trait KebsJsonSchema {
  import macros.KebsJsonSchemaMacros
  implicit val jswUnit: JsonSchemaWrapper[Unit] = JsonSchemaWrapper[Unit](null)

  implicit def ValueClassLikeJsonSchemaPredef[T, A](implicit rep: ValueClassLike[T, A],
                                                   schema: json.schema.Predef[A]): json.schema.Predef[T] =
    schema.asInstanceOf[json.schema.Predef[T]]
  implicit def genericJsonSchemaWrapper[T]: JsonSchemaWrapper[T] = macro KebsJsonSchemaMacros.materializeSchema[T]
}
