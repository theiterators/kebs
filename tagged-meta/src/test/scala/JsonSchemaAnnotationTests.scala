import com.github.andyglow.json.JsonFormatter
import com.github.andyglow.jsonschema.AsValue
import json.Schema
import json.schema.Version.Draft07
import pl.iterators.kebs.tagged._
import pl.iterators.kebs.tag.meta._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.jsonschema.{JsonSchemaWrapper, KebsJsonSchema}

@tagged object JsonSchemaTestTags {
  trait NameTag
  trait IdTag[+A]
  trait PositiveIntTag

  type Name  = String @@ NameTag
  type Id[A] = Int @@ IdTag[A]

  type PositiveInt = Int @@ PositiveIntTag
  object PositiveInt {
    sealed trait Error
    case object Negative extends Error
    case object Zero     extends Error

    def validate(i: Int): Either[Error, Int] = if (i == 0) Left(Zero) else if (i < 0) Left(Negative) else Right(i)
  }
}

@tagged trait JsonSchemaTestTagsTrait {
  trait OrdinaryTag
  trait NegativeIntTag

  type Ordinary = String @@ OrdinaryTag

  type NegativeInt = Int @@ NegativeIntTag
  object NegativeInt {
    sealed trait Error
    case object Positive extends Error
    case object Zero     extends Error

    def validate(i: Int) = if (i == 0) Left(Zero) else if (i > 0) Left(Positive) else Right(i)
  }
}

object JsonSchemaTestTagsFromTrait extends JsonSchemaTestTagsTrait

class JsonSchemaAnnotationTests extends AnyFunSuite with Matchers with KebsJsonSchema {
  test("json schema implicits are generated (object)") {
    import JsonSchemaTestTags._
    implicitly[JsonSchemaWrapper[Name]].schema shouldEqual Schema.string
  }

  test("spray implicits are generated (trait)") {
    import JsonSchemaTestTagsFromTrait._
    implicitly[JsonSchemaWrapper[Ordinary]].schema shouldEqual Schema.string
  }

  test("spray implicits for generic tags are generated") {
    import JsonSchemaTestTags._
    trait Marker
    implicitly[JsonSchemaWrapper[Id[Marker]]].schema shouldEqual Schema.integer
  }

  case class C(i: Int, j: JsonSchemaTestTags.PositiveInt)
  case class C2(i: Int, j: Int)
  test("Implicits are found from tag companion object") {
    val schema1: Schema[C]  = implicitly[JsonSchemaWrapper[C]].schema
    val schema2: Schema[C2] = json.Json.schema[C2]
    schema1 shouldEqual schema2
  }
}
