package pl.iterators.kebs.playjson.golden

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.enumeratum.{KebsEnumeratum, KebsValueEnumeratum}
import pl.iterators.kebs.instances.TimeInstances
import pl.iterators.kebs.playjson.KebsPlayJson
import pl.iterators.kebs.testkit.model._
import play.api.libs.json._

import java.time.Instant

/** What kebs 2.x provides for play-json (flat value classes, enums, instance converters) next to user-written `Json.format` codecs. kebs 3
  * adds case-class derivation; these codecs must keep winning and keep this output.
  */
class PlayJsonGoldenTests extends AnyFunSuite with Matchers {
  private def golden[A](value: A, expected: String)(implicit format: Format[A]): Unit = {
    format.writes(value) shouldBe Json.parse(expected)
    format.reads(Json.parse(expected)) shouldBe JsSuccess(value)
  }

  object Flat extends KebsPlayJson with CaseClass1ToValueClass with KebsEnumeratum with KebsValueEnumeratum with TimeInstances

  test("flat value class") {
    import Flat._
    golden(Id("x"), "\"x\"")
  }

  test("user Json.format with kebs field formats") {
    import Flat._
    implicit val personFormat: OFormat[Person] = Json.format[Person]
    golden(Person(Id("x"), "Ann", 30, None), """{"id":"x","name":"Ann","age":30}""")
  }

  test("enums") {
    import Flat._
    import pl.iterators.kebs.playjson.enums._
    implicit val paintFormat: OFormat[Paint] = Json.format[Paint]
    golden(Paint(Color.DarkBlue, Level.High), """{"color":"DarkBlue","level":2}""")
  }

  test("enums uppercase") {
    import Flat._
    import pl.iterators.kebs.playjson.enums.uppercase._
    implicit val paintFormat: OFormat[Paint] = Json.format[Paint]
    golden(Paint(Color.DarkBlue, Level.High), """{"color":"DARKBLUE","level":2}""")
  }

  test("enums lowercase") {
    import Flat._
    import pl.iterators.kebs.playjson.enums.lowercase._
    implicit val paintFormat: OFormat[Paint] = Json.format[Paint]
    golden(Paint(Color.DarkBlue, Level.High), """{"color":"darkblue","level":2}""")
  }

  test("instance converter") {
    import Flat._
    implicit val eventFormat: OFormat[Event] = Json.format[Event]
    golden(Event(Id("e"), Instant.parse("2026-01-02T03:04:05Z")), """{"id":"e","at":"2026-01-02T03:04:05Z"}""")
  }
}
