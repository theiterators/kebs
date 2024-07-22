package pl.iterators.kebs.jsonschema.macros

import scala.reflect.macros._
import pl.iterators.kebs.core.macros.MacroUtils
import pl.iterators.kebs.jsonschema.JsonSchemaWrapper

class KebsJsonSchemaMacros(override val c: whitebox.Context) extends MacroUtils {
  import c.universe._

  final def materializeSchema[T: c.WeakTypeTag]: c.Expr[JsonSchemaWrapper[T]] = {
    val T = weakTypeOf[T]
    val tree =
      q"""{
         val schema = _root_.json.Json.schema[$T]
         _root_.pl.iterators.kebs.jsonschema.JsonSchemaWrapper(schema)
         }"""
    c.Expr[JsonSchemaWrapper[T]](tree)
  }
}
