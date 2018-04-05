import org.scalatest._
import pl.iterators.kebs.tag.@@
import pl.iterators.kebs.tag.meta.tagged

@tagged object TestTags {
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

class TaggedAnnotationTests extends FunSuite with Matchers with EitherValues {
  import TestTags._
  test("apply and from methods are generated") {
    Name.from("Someone") shouldEqual "Someone"
    Name("Someone") shouldEqual "Someone"
  }

  test("apply and from method can be generic") {
    trait Marker
    Id.from[Marker](10) shouldEqual 10
    Id[Marker](10) shouldEqual 10
  }

  test("from method must use validation") {
    PositiveInt.from(10).right.value shouldEqual 10
    PositiveInt.from(0).left.value shouldEqual PositiveInt.Zero
  }

  test("apply method must throw exception if validation failed") {
    an[IllegalArgumentException] shouldBe thrownBy(PositiveInt(-10))
  }
}
