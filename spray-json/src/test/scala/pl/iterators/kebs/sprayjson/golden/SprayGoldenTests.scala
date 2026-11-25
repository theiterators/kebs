package pl.iterators.kebs.sprayjson.golden

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.enumeratum.{KebsEnumeratum, KebsValueEnumeratum}
import pl.iterators.kebs.instances.TimeInstances
import pl.iterators.kebs.sprayjson.{KebsSprayJson, KebsSprayJsonEnums, KebsSprayJsonValueEnums}
import pl.iterators.kebs.sprayjson.enums.{KebsSprayJsonEnumsLowercase, KebsSprayJsonEnumsUppercase}
import pl.iterators.kebs.testkit.model._
import spray.json._

import java.time.Instant

object SprayGoldenTestDomain {
  final case class LibraryCases(some: Some[Int], right: Right[Int, String], left: Left[String, Int])
}

// kebs 2.x wire format (verified against v2.3.0); a deliberate change goes to the migration guide, not into these expectations
class SprayGoldenTests extends AnyFunSuite with Matchers {
  private def golden[A](value: A, expected: String)(implicit format: JsonFormat[A]): Unit = {
    format.write(value) shouldBe expected.parseJson
    format.read(expected.parseJson) shouldBe value
  }

  object Default   extends DefaultJsonProtocol with KebsSprayJson
  object Flat      extends DefaultJsonProtocol with KebsSprayJson with CaseClass1ToValueClass
  object FlatNulls extends DefaultJsonProtocol with KebsSprayJson with CaseClass1ToValueClass with NullOptions
  object FlatTime  extends DefaultJsonProtocol with KebsSprayJson with CaseClass1ToValueClass with TimeInstances
  object FlatEnums
      extends DefaultJsonProtocol
      with KebsSprayJson
      with CaseClass1ToValueClass
      with KebsEnumeratum
      with KebsValueEnumeratum
      with KebsSprayJsonEnums
      with KebsSprayJsonValueEnums
  object FlatEnumsUppercase
      extends DefaultJsonProtocol
      with KebsSprayJson
      with CaseClass1ToValueClass
      with KebsEnumeratum
      with KebsValueEnumeratum
      with KebsSprayJsonEnumsUppercase
      with KebsSprayJsonValueEnums
  object FlatEnumsLowercase
      extends DefaultJsonProtocol
      with KebsSprayJson
      with CaseClass1ToValueClass
      with KebsEnumeratum
      with KebsValueEnumeratum
      with KebsSprayJsonEnumsLowercase
      with KebsSprayJsonValueEnums

  test("person default") {
    import Default._
    golden(Person(Id("x"), "Ann", 30, None), """{"id":{"value":"x"},"name":"Ann","age":30}""")
  }

  test("member order, as printed (spray sorts members)") {
    import Flat._
    implicitly[JsonFormat[Person]].write(Person(Id("x"), "Ann", 30, Some("A"))).compactPrint shouldBe
    """{"age":30,"id":"x","name":"Ann","nickname":"A"}"""
  }

  test("person flat") {
    import Flat._
    golden(Person(Id("x"), "Ann", 30, Some("A")), """{"id":"x","name":"Ann","age":30,"nickname":"A"}""")
  }

  test("person NullOptions") {
    import FlatNulls._
    golden(Person(Id("x"), "Ann", 30, None), """{"id":"x","name":"Ann","age":30,"nickname":null}""")
  }

  test("naming snakified") {
    import pl.iterators.kebs.sprayjson.snakified._
    golden(Naming(1, "a", 2, "u"), """{"some_field":1,"another_field_name":"a","field1_name":2,"http_url":"u"}""")
  }

  test("naming capitalized") {
    import pl.iterators.kebs.sprayjson.capitalized._
    golden(Naming(1, "a", 2, "u"), """{"SomeField":1,"AnotherFieldName":"a","Field1Name":2,"HttpURL":"u"}""")
  }

  test("enums") {
    import FlatEnums._
    golden(Paint(Color.DarkBlue, Level.High), """{"color":"DarkBlue","level":2}""")
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

  test("cases of the standard library's sums") {
    import Default._
    import SprayGoldenTestDomain._
    golden(
      LibraryCases(Some(5), Right("r"), Left("l")),
      """{"some":{"value":5},"right":{"value":"r"},"left":{"value":"l"}}"""
    )
    implicitly[JsonFormat[List[Some[Int]]]].write(List(Some(5))).compactPrint shouldBe """[{"value":5}]"""
  }

  test("wide (>22 fields)") {
    import Default._
    golden(
      Wide.sample,
      """{"f1":1,"f2":2,"f3":3,"f4":4,"f5":5,"f6":6,"f7":7,"f8":8,"f9":9,"f10":10,"f11":11,"f12":12,"f13":13,"f14":14,"f15":15,"f16":16,"f17":17,"f18":18,"f19":19,"f20":20,"f21":21,"f22":22}"""
    )
  }

  test("wide NullOptions") {
    import FlatNulls._
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
