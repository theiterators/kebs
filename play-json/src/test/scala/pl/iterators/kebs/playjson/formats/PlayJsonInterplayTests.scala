package pl.iterators.kebs.playjson.formats

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.playjson.KebsPlayJson
import pl.iterators.kebs.testkit.model._
import play.api.libs.json._

object PlayJsonInterplayTestDomain {
  final case class Spot(x: Int, y: Int)
  final case class Pinned(name: String, spot: Spot)
  final case class PinnedByPlay(label: String, spot: Spot)
}

class PlayJsonInterplayTests extends AnyFunSuite with Matchers {
  import PlayJsonInterplayTestDomain._

  object Default extends KebsPlayJson
  object Flat    extends KebsPlayJson with CaseClass1ToValueClass

  private val spotFormat: Format[Spot] = Format(
    Reads.StringReads.map { s =>
      val Array(x, y) = s.split(',')
      Spot(x.toInt, y.toInt)
    },
    Writes.StringWrites.contramap(s => s"${s.x},${s.y}")
  )

  test("Format and OFormat requests resolve through play's combinators") {
    import Default._
    val person = Person(Id("x"), "Ann", 30, Some("A"))
    val json   = Json.parse("""{"id":{"value":"x"},"name":"Ann","age":30,"nickname":"A"}""")
    implicitly[Format[Person]].writes(person) shouldBe json
    implicitly[OFormat[Person]].writes(person) shouldBe json
    implicitly[OFormat[Person]].reads(json) shouldBe JsSuccess(person)
  }

  test("a local Format wins at the root, in a derived type and in a Json.format") {
    import Default._
    implicit val local: Format[Spot] = spotFormat
    Json.toJson(Spot(1, 2)) shouldBe JsString("1,2")
    Json.toJson(Pinned("p", Spot(1, 2))) shouldBe Json.parse("""{"name":"p","spot":"1,2"}""")
    Json.parse("""{"name":"p","spot":"1,2"}""").validate[Pinned] shouldBe JsSuccess(Pinned("p", Spot(1, 2)))
    val byPlay = Json.format[PinnedByPlay]
    byPlay.writes(PinnedByPlay("l", Spot(3, 4))) shouldBe Json.parse("""{"label":"l","spot":"3,4"}""")
  }

  test("a local Reads of the same type as kebs' entry point wins, without ambiguity") {
    import Default._
    implicit val localReads: Reads[Spot] = spotFormat
    implicitly[Reads[Spot]].reads(JsString("5,6")) shouldBe JsSuccess(Spot(5, 6))
  }

  test("value classes are flat as Reads, Writes and Format") {
    import Flat._
    implicitly[Reads[Id]].reads(JsString("x")) shouldBe JsSuccess(Id("x"))
    implicitly[Writes[Id]].writes(Id("x")) shouldBe JsString("x")
    implicitly[Format[Id]].writes(Id("x")) shouldBe JsString("x")
    Json.toJson(Person(Id("x"), "Ann", 30, None)) shouldBe Json.parse("""{"id":"x","name":"Ann","age":30}""")
  }

  test("a protocol mixed into a class") {
    class Controller extends KebsPlayJson {
      def render(pinned: Pinned): JsValue = Json.toJson(pinned)
    }
    new Controller().render(Pinned("p", Spot(1, 2))) shouldBe Json.parse("""{"name":"p","spot":{"x":1,"y":2}}""")
  }
}
