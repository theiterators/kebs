package pl.iterators.kebs.circe.golden

import io.circe.{Decoder, Encoder, Json}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.circe.KebsCirce
import pl.iterators.kebs.circe.enums.{KebsCirceEnums, KebsCirceEnumsLowercase, KebsCirceEnumsUppercase, KebsCirceValueEnums}
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.enumeratum.{KebsEnumeratum, KebsValueEnumeratum}
import pl.iterators.kebs.instances.TimeInstances
import pl.iterators.kebs.testkit.model._

import java.time.Instant

/** Wire format of kebs 2.x (captured before the kebs 3 rewrite). kebs 3 must produce the same JSON with default configuration; a
  * deliberate change belongs in the migration guide, not in a silently edited expectation.
  */
class CirceGoldenTests extends AnyFunSuite with Matchers {
  private def json(s: String): Json = io.circe.parser.parse(s).fold(throw _, identity)

  private def golden[A](value: A, expected: String)(implicit encoder: Encoder[A], decoder: Decoder[A]): Unit = {
    encoder(value) shouldBe json(expected)
    decoder.decodeJson(json(expected)) shouldBe Right(value)
  }

  object Default  extends KebsCirce
  object Flat     extends KebsCirce with CaseClass1ToValueClass
  object FlatTime extends KebsCirce with CaseClass1ToValueClass with TimeInstances
  object FlatEnums
      extends KebsCirce
      with CaseClass1ToValueClass
      with KebsEnumeratum
      with KebsValueEnumeratum
      with KebsCirceEnums
      with KebsCirceValueEnums
  object FlatEnumsUppercase
      extends KebsCirce
      with CaseClass1ToValueClass
      with KebsEnumeratum
      with KebsValueEnumeratum
      with KebsCirceEnumsUppercase
      with KebsCirceValueEnums
  object FlatEnumsLowercase
      extends KebsCirce
      with CaseClass1ToValueClass
      with KebsEnumeratum
      with KebsValueEnumeratum
      with KebsCirceEnumsLowercase
      with KebsCirceValueEnums

  test("person default") {
    import Default._
    golden(Person(Id("x"), "Ann", 30, None), """{"id":{"value":"x"},"name":"Ann","age":30,"nickname":null}""")
  }

  test("person flat") {
    import Flat._
    golden(Person(Id("x"), "Ann", 30, Some("A")), """{"id":"x","name":"Ann","age":30,"nickname":"A"}""")
  }

  test("naming snakified") {
    import pl.iterators.kebs.circe.snakified._
    golden(Naming(1, "a", 2, "u"), """{"some_field":1,"another_field_name":"a","field1_name":2,"http_url":"u"}""")
  }

  test("naming capitalized") {
    import pl.iterators.kebs.circe.capitalized._
    golden(Naming(1, "a", 2, "u"), """{"SomeField":1,"AnotherFieldName":"a","Field1Name":2,"HttpURL":"u"}""")
  }

  test("enums") {
    import FlatEnums._
    golden(Paint(Color.DarkBlue, Level.High), """{"color":"DarkBlue","level":2}""")
  }

  test("enums are decoded case-insensitively by default") {
    import FlatEnums._
    implicitly[Decoder[Paint]].decodeJson(json("""{"color":"darkblue","level":2}""")) shouldBe Right(Paint(Color.DarkBlue, Level.High))
  }

  test("enums uppercase") {
    import FlatEnumsUppercase._
    golden(Paint(Color.DarkBlue, Level.High), """{"color":"DARKBLUE","level":2}""")
  }

  test("enums lowercase") {
    import FlatEnumsLowercase._
    golden(Paint(Color.DarkBlue, Level.High), """{"color":"darkblue","level":2}""")
  }

  test("instance converter") {
    import FlatTime._
    golden(Event(Id("e"), Instant.parse("2026-01-02T03:04:05Z")), """{"id":"e","at":"2026-01-02T03:04:05Z"}""")
  }

  test("wide (>22 fields)") {
    import Default._
    golden(
      Wide.sample,
      """{"f1":1,"f2":2,"f3":3,"f4":4,"f5":5,"f6":6,"f7":7,"f8":8,"f9":9,"f10":10,"f11":11,"f12":12,"f13":13,"f14":14,"f15":15,"f16":16,"f17":17,"f18":18,"f19":19,"f20":20,"f21":21,"f22":22,"f23":null}"""
    )
  }

  test("private-constructor value class (flat)") {
    import Flat._
    golden(Contact(Email.from("a@b.pl"), "n"), """{"email":"a@b.pl","name":"n"}""")
  }
}
