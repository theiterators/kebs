package pl.iterators.kebs.jsonschema

import com.github.andyglow.json.JsonFormatter
import com.github.andyglow.jsonschema.AsValue
import json.schema.Version.Draft07
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class JsonSchemaTests extends AnyFunSuite with Matchers {
  object KebsProtocol extends KebsJsonSchema

  object SchemaPrinter {
    def printWrapper[T](id: String = "id")(implicit schemaWrapper: JsonSchemaWrapper[T]): String =
      JsonFormatter.format(AsValue.schema(schemaWrapper.schema, Draft07(id)))
  }

  test("Basic test") {
    import KebsProtocol._
    "implicitly[JsonSchemaWrapper[Sample]]" should compile
  }
}
