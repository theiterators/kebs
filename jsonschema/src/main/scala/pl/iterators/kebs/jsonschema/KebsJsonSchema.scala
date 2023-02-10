package pl.iterators.kebs.jsonschema

import pl.iterators.kebs.macros.base.CaseClass1Rep

trait KebsJsonSchema {
  import macros.KebsJsonSchemaMacros
  implicit val jswUnit: JsonSchemaWrapper[Unit] = JsonSchemaWrapper[Unit](null)

  implicit def caseClass1RepJsonSchemaPredef[T, A](implicit rep: CaseClass1Rep[T, A],
                                                   schema: json.schema.Predef[A]): json.schema.Predef[T] =
    schema.asInstanceOf[json.schema.Predef[T]]
  implicit def genericJsonSchemaWrapper[T]: JsonSchemaWrapper[T] = macro KebsJsonSchemaMacros.materializeSchema[T]
}
