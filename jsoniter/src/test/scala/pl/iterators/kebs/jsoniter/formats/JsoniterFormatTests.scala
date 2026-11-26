package pl.iterators.kebs.jsoniter.formats

import com.github.plokhotnyuk.jsoniter_scala.core._
import pl.iterators.kebs.jsoniter.KebsJsoniter
import pl.iterators.kebs.testkit.model._
import pl.iterators.kebs.testkit.nested.{Gen, Item}

object JsoniterFormatTestDomain {
  final case class Single(value: Int)
  final case class NoFields()
  final case class Percent private (value: Int, label: String)
  object Percent {
    def apply(value: Int, label: String): Percent = {
      require(value <= 100, "more than all")
      new Percent(value, label.toUpperCase)
    }
  }
  final case class Nested(inner: Single, others: List[Single], byName: Map[String, Single], maybe: Option[Single])
  final case class Reserved(`type`: Int, `some field`: String, + : Int)
}

class JsoniterFormatTests extends JsoniterTestBase {
  import JsoniterFormatTestDomain._

  object Protocol extends KebsJsoniter
  import Protocol.deriveCodec

  test("case class") {
    implicit val codec: JsonValueCodec[Person] = deriveCodec[Person]
    roundTrip(Person(Id("x"), "Ann", 30, Some("a")), """{"id":{"value":"x"},"name":"Ann","age":30,"nickname":"a"}""")
    roundTrip(Person(Id("x"), "Ann", 30, None), """{"id":{"value":"x"},"name":"Ann","age":30,"nickname":null}""")
    readFromString[Person]("""{"id":{"value":"x"},"name":"Ann","age":30}""") shouldBe Person(Id("x"), "Ann", 30, None)
  }

  test("one field, no fields") {
    roundTrip(Single(1), """{"value":1}""")(deriveCodec[Single])
    roundTrip(NoFields(), "{}")(deriveCodec[NoFields])
    readFromString[NoFields]("""{"a":[{}]}""")(deriveCodec[NoFields]) shouldBe NoFields()
  }

  test("more than 22 fields") {
    implicit val codec: JsonValueCodec[Wide] = deriveCodec[Wide]
    readFromString[Wide](writeToString(Wide.sample)) shouldBe Wide.sample
  }

  test("nested case classes, in containers too") {
    roundTrip(
      Nested(Single(1), List(Single(2)), Map("a" -> Single(3)), Some(Single(4))),
      """{"inner":{"value":1},"others":[{"value":2}],"byName":{"a":{"value":3}},"maybe":{"value":4}}"""
    )(deriveCodec[Nested])
  }

  test("generic case class") {
    roundTrip(Gen(Item("i", 1), 2), """{"x":{"name":"i","n":1},"n":2}""")(deriveCodec[Gen[Item]])
    roundTrip(Gen(List(1), 2), """{"x":[1],"n":2}""")(deriveCodec[Gen[List[Int]]])
  }

  test("a companion's apply is used") {
    implicit val codec: JsonValueCodec[Percent] = deriveCodec[Percent]
    readFromString[Percent]("""{"value":1,"label":"a"}""").label shouldBe "A"
    (the[JsonReaderException] thrownBy readFromString[Percent]("""{"value":101,"label":"a"}""")).getMessage should include("more than all")
  }

  test("field names that are not identifiers") {
    roundTrip(Reserved(1, "a", 2), """{"type":1,"some field":"a","+":2}""")(deriveCodec[Reserved])
  }

  test("a container of case classes") {
    roundTrip(List(Single(1)), """[{"value":1}]""")(deriveCodec[List[Single]])
    roundTrip(Map("a" -> Single(1)), """{"a":{"value":1}}""")(deriveCodec[Map[String, Single]])
  }
}
