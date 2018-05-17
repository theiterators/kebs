import org.scalatest.{FunSuite, Matchers}
import pl.iterators.kebs.tagged.@@
import pl.iterators.kebs.tag.meta._
import _root_.spray.json._

@tagged @spray object SprayTestTags {
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

    def validate(i: Int) = if (i == 0) Left(Zero) else if (i < 0) Left(Negative) else Right(i)
  }
}

@tagged @spray trait SprayTestTagsTrait {
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

object SprayTestTagsFromTrait extends SprayTestTagsTrait

class SprayAnnotationTests extends FunSuite with Matchers {
  test("spray implicits are generated (object)") {
    import SprayTestTags._
    implicitly[JsonReader[Name]].read(JsString("Joe")) shouldEqual "Joe"
  }

  test("spray implicits are generated (trait)") {
    import SprayTestTagsFromTrait._
    implicitly[JsonReader[Ordinary]].read(JsString("Joe")) shouldEqual "Joe"
  }

  test("spray implicits for generic tags are generated") {
    import SprayTestTags._
    trait Marker
    implicitly[JsonReader[Id[Marker]]].read(JsNumber(10)) shouldEqual 10
  }

  test("generated implicits use validation (object)") {
    import SprayTestTags._
    val reader = implicitly[JsonReader[PositiveInt]]
    an[DeserializationException] shouldBe thrownBy(reader.read(JsNumber(-10)))
    reader.read(JsNumber(10)) shouldEqual 10
  }

  test("generated implicits use validation (trait)") {
    import SprayTestTagsFromTrait._
    val reader = implicitly[JsonReader[NegativeInt]]
    an[DeserializationException] shouldBe thrownBy(reader.read(JsNumber(10)))
    reader.read(JsNumber(-10)) shouldEqual -10
  }

  case class C(i: Int, j: SprayTestTags.PositiveInt)
  test("Implicits are found from tag companion object") {
    import DefaultJsonProtocol._
    val format: JsonFormat[C] = jsonFormat2(C.apply)
    format.read(JsObject("i" -> JsNumber(1), "j" -> JsNumber(2))).j shouldEqual 2
  }
}
