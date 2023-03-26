import io.circe.{Decoder, Encoder, Json}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.circe.KebsCirce
import io.circe.parser.parse
import io.circe.derivation.ConfiguredDecoder
import io.circe.derivation.Configuration
import model.ClassWith23Fields


class CirceFormatNoFlatTests extends AnyFunSuite with Matchers {
  object KebsProtocol extends KebsCirce with KebsCirce.NoFlat
  import KebsProtocol._

case class C(i: Int)

  test("No-flat format") {
    val decoder = implicitly[Decoder[C]]
    val encoder = implicitly[Encoder[C]]
    decoder.apply(Json.fromFields(Seq("i" -> Json.fromInt(10))).hcursor) shouldBe Right(C(10))
    encoder.apply(C(10)) shouldBe Json.fromFields(Seq("i" -> Json.fromInt(10)))
  }

  case class Book(name: String, chapters: List[Chapter])
  case class Chapter(name: String)

  test("compound") {
    val decoder = implicitly[Decoder[Book]]
    val json =
      """
        | {
        |   "name": "Functional Programming in Scala",
        |   "chapters": [{"name":"first"}, {"name":"second"}]
        | }
      """.stripMargin
    val Right(book) = parse(json)
    decoder(book.hcursor) shouldBe Right(
      Book(
        name = "Functional Programming in Scala",
        chapters = List(Chapter("first"), Chapter("second"))
      ))
  }
}
