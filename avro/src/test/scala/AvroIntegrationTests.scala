import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.time.Instant

import com.sksamuel.avro4s._
import org.apache.avro.Schema
import org.scalatest.{FunSuite, Matchers}
import pl.iterators.kebs.avro.AvroKebs

case class Ingredient(name: String) extends AnyVal
case class Pizza(name: String, ingredients: Seq[Ingredient], vegetarian: Boolean, vegan: Boolean, calories: Int)

case class CustomerDates(activatedAt: Option[CustomerActivatedAt],
                         updatedAt: Option[CustomerUpdatedAt],
                         deletedAt: Option[CustomerDeletedAt])

case class CustomerActivatedAt(value: Instant) extends AnyVal
case class CustomerUpdatedAt(value: Instant)   extends AnyVal
case class CustomerDeletedAt(value: Instant)   extends AnyVal

case class Customer(phoneNumber: Option[PhoneNumber])
case class PhoneNumber(value: String) extends AnyVal

class AvroIntegrationTests extends FunSuite with Matchers with AvroKebs {
  test("Generate schema with value types") {
    val schema            = AvroSchema[Pizza]
    val ingredientsField  = schema.getField("ingredients")
    val ingredientsSchema = ingredientsField.schema()

    ingredientsSchema.getElementType shouldEqual Schema.create(Schema.Type.STRING)
  }

  test("Generate schema with custom value types") {
    import scala.collection.JavaConverters._
    implicit val schemaForInstant: ToSchema[Instant] = new ToSchema[Instant] {
      override protected val schema = Schema.create(Schema.Type.LONG)
    }
    implicit val instantToValue: ToValue[Instant] = new ToValue[Instant] {
      override def apply(value: Instant) = value.toEpochMilli
    }
    implicit val instantFromValue: FromValue[Instant] = new FromValue[Instant] {
      override def apply(value: Any, field: Schema.Field) = Instant.ofEpochMilli(value.asInstanceOf[Long])
    }
    val schema            = AvroSchema[CustomerDates]
    val activatedAtField  = schema.getField("activatedAt")
    val activatedAtSchema = activatedAtField.schema()

    activatedAtSchema.getType shouldEqual Schema.Type.UNION
    activatedAtSchema.getTypes.asScala should contain theSameElementsAs List(Schema.create(Schema.Type.NULL),
                                                                             Schema.create(Schema.Type.LONG))
  }

  test("Serialize and deserialize value types correctly") {
    val baos   = new ByteArrayOutputStream()
    val output = AvroOutputStream.binary[Customer](baos)
    output.write(Customer(Some(PhoneNumber("+1-999-999-999"))))
    output.write(Customer(None))
    output.close()
    val bytes = baos.toByteArray

    val in     = new ByteArrayInputStream(bytes)
    val input  = AvroInputStream.binary[Customer](in)
    val result = input.iterator.toSeq
    result shouldBe Vector(Customer(Some(PhoneNumber("+1-999-999-999"))), Customer(None))
  }
}
