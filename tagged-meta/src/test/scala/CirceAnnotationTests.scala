import io.circe.{CursorOp, Decoder, DecodingFailure, Json}
import org.scalatest.funsuite.AnyFunSuite
import pl.iterators.kebs.tagged._
import pl.iterators.kebs.tag.meta._
import pl.iterators.kebs.circe.KebsCirce
import org.scalatest.matchers.should.Matchers

@tagged object CirceTestTags {
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

@tagged trait CirceTestTagsTrait {
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

object CirceTestTagsFromTrait extends CirceTestTagsTrait

class CirceAnnotationTests extends AnyFunSuite with Matchers with KebsCirce {
  test("circe implicits are generated (object)") {
    import CirceTestTags._
    implicitly[Decoder[Name]].apply(Json.fromString("Joe").hcursor) shouldEqual Right("Joe")
  }

  test("circe implicits are generated (trait)") {
    import CirceTestTagsFromTrait._
    implicitly[Decoder[Ordinary]].apply(Json.fromString("Joe").hcursor) shouldEqual Right("Joe")
  }

  test("spray implicits for generic tags are generated") {
    import CirceTestTags._
    trait Marker
    implicitly[Decoder[Id[Marker]]].apply(Json.fromInt(10).hcursor) shouldEqual Right(10)
  }

  test("generated implicits use validation (object)") {
    import CirceTestTags._
    val decoder = implicitly[Decoder[PositiveInt]]
    decoder.apply(Json.fromInt(-10).hcursor) shouldEqual Left(DecodingFailure(PositiveInt.Negative.toString, List.empty[CursorOp]))
    decoder.apply(Json.fromInt(10).hcursor) shouldEqual Right(10)
  }

  test("generated implicits use validation (trait)") {
    import CirceTestTagsFromTrait._
    val decoder = implicitly[Decoder[NegativeInt]]
    decoder.apply(Json.fromInt(10).hcursor) shouldEqual Left(DecodingFailure(NegativeInt.Positive.toString, List.empty[CursorOp]))
    decoder.apply(Json.fromInt(-10).hcursor) shouldEqual Right(-10)
  }

  case class C(i: Int, j: CirceTestTags.PositiveInt)
  test("Implicits are found from tag companion object") {
    val decoder: Decoder[C] = implicitly[Decoder[C]]
    decoder.apply(Json.fromFields(List("i" -> Json.fromInt(1), "j" -> Json.fromInt(2))).hcursor).right.map(_.j) shouldEqual Right(2)
  }
}
