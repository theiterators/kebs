import com.github.andyglow.json.JsonFormatter
import com.github.andyglow.jsonschema.AsValue
import json.schema.Version.Draft07
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.jsonschema.{KebsJsonSchema, JsonSchemaWrapper}

case class WrappedInt(int: Int)
case class WrappedIntAnyVal(int: Int) extends AnyVal
case class Sample(someNumber: Int,
                  someText: String,
                  arrayOfNumbers: List[Int],
                  wrappedNumber: WrappedInt,
                  wrappedNumberAnyVal: WrappedIntAnyVal)

class JsonSchemaTests extends AnyFunSuite with Matchers {
  object KebsProtocol extends KebsJsonSchema

  object SchemaPrinter {
    def printWrapper[T](id: String = "id")(implicit schemaWrapper: JsonSchemaWrapper[T]): String =
      JsonFormatter.format(AsValue.schema(schemaWrapper.schema, Draft07(id)))
  }

  test("Basic test") {
    import KebsProtocol._
    print(SchemaPrinter.printWrapper[Sample]())
    1 shouldBe 1
  }
}
