import com.sksamuel.avro4s.ToSchema
import org.apache.avro.Schema
import org.scalatest.{FunSuite, Matchers}

class ToSchemaTests extends FunSuite with Matchers {
  import ToSchemaTests._
  import pl.iterators.kebs.avro._

  test("Materialize ToSchema for simple value type") {
    val ToSchema = implicitly[ToSchema[S]]
    ToSchema().getType shouldEqual Schema.Type.STRING
  }

  test("Materialize ToSchema for option of simple value type") {
    import scala.collection.JavaConverters._

    val ToSchema = implicitly[ToSchema[Option[S]]]
    val schema   = ToSchema()

    schema.getType shouldEqual Schema.Type.UNION
    schema.getTypes shouldEqual List(Schema.create(Schema.Type.NULL), Schema.create(Schema.Type.STRING)).asJava
  }

  test("Materialize ToSchema for compound value type") {
    val ToSchema = implicitly[ToSchema[V]]
    val schema   = ToSchema()

    schema.getType shouldEqual Schema.Type.ARRAY
    schema.getElementType shouldEqual Schema.create(Schema.Type.STRING)
  }

  case class NotAnyVal(i: Int)
  test("Do not materialize ToSchema for ref type") {
    val ToSchema = implicitly[ToSchema[NotAnyVal]]
    ToSchema().getType shouldEqual Schema.Type.RECORD
  }

}

object ToSchemaTests {
  case class S(s: String)         extends AnyVal
  case class V(v: Vector[String]) extends AnyVal
}
