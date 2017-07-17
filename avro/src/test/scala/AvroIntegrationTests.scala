import com.sksamuel.avro4s._
import org.apache.avro.Schema
import org.scalatest.{FunSuite, Matchers}
import pl.iterators.kebs.avro.AvroKebs

case class Ingredient(name: String) extends AnyVal
case class Pizza(name: String, ingredients: Seq[Ingredient], vegetarian: Boolean, vegan: Boolean, calories: Int)

class AvroIntegrationTests extends FunSuite with Matchers with AvroKebs {
  test("Generate schema with value types") {
    val schema            = AvroSchema[Pizza]
    val ingredientsField  = schema.getField("ingredients")
    val ingredientsSchema = ingredientsField.schema()

    ingredientsSchema.getElementType shouldEqual Schema.create(Schema.Type.STRING)
  }
}
