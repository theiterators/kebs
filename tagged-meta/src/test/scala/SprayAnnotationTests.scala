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
  import SprayTestTags._
  test("spray implicits are generated") {
    implicitly[JsonReader[Name]].read(JsString("Joe")) shouldEqual "Joe"
  }

  test("spray implicits for generic tags are generated") {
    trait Marker
    implicitly[JsonReader[Id[Marker]]].read(JsNumber(10)) shouldEqual 10
  }

  test("generated implicits use validation") {
    val reader = implicitly[JsonReader[PositiveInt]]
    an[DeserializationException] shouldBe thrownBy(reader.read(JsNumber(-10)))
    reader.read(JsNumber(10)) shouldEqual 10
  }
}
