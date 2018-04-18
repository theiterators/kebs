import org.scalatest.{FunSuite, Matchers}
import pl.iterators.kebs.tag.@@
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

class SprayAnnotationTests extends FunSuite with Matchers {
  test("spray implicits are generated") {
    import SprayTestTags._
    implicitly[JsonReader[Name]].read(JsString("Joe")) shouldEqual "Joe"
  }

  test("spray implicits for generic tags are generated") {
    import SprayTestTags._
    trait Marker
    implicitly[JsonReader[Id[Marker]]].read(JsNumber(10)) shouldEqual 10
  }

  test("generated implicits use validation") {
    import SprayTestTags._
    val reader = implicitly[JsonReader[PositiveInt]]
    an[DeserializationException] shouldBe thrownBy(reader.read(JsNumber(-10)))
    reader.read(JsNumber(10)) shouldEqual 10
  }

  case class C(i: Int, j: SprayTestTags.PositiveInt)
  test("Implicits are found from tag companion object") {
    import DefaultJsonProtocol._
    val format: JsonFormat[C] = jsonFormat2(C.apply)
    format.read(JsObject("i" -> JsNumber(1), "j" -> JsNumber(2))).j shouldEqual 2
  }
}
