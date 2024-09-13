package pl.iterators.kebs.tag.meta

import org.scalatest._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.tagged._

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

@tagged trait TestTagsTrait {
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

object TestTagsFromTrait extends TestTagsTrait

class TaggedAnnotationTests extends AnyFunSuite with Matchers with EitherValues {
  import TestTags._
  import TestTagsFromTrait._

  test("apply and from methods are generated (object)") {
    Name.from("Someone") shouldEqual "Someone"
    Name("Someone") shouldEqual "Someone"
  }

  test("apply and from methods are generated (trait)") {
    Ordinary.from("Someone") shouldEqual "Someone"
    Ordinary("Someone") shouldEqual "Someone"
  }

  test("apply and from method can be generic") {
    trait Marker
    Id.from[Marker](10) shouldEqual 10
    Id[Marker](10) shouldEqual 10
  }

  test("from method must use validation (object)") {
    PositiveInt.from(10).value shouldEqual 10
    PositiveInt.from(0).left.value shouldEqual PositiveInt.Zero
  }

  test("from method must use validation (trait)") {
    NegativeInt.from(-10).value shouldEqual -10
    NegativeInt.from(0).left.value shouldEqual NegativeInt.Zero
  }

  test("apply method must throw exception if validation failed (object)") {
    an[IllegalArgumentException] shouldBe thrownBy(PositiveInt(-10))
  }

  test("apply method must throw exception if validation failed (trait)") {
    an[IllegalArgumentException] shouldBe thrownBy(NegativeInt(10))
  }
}
