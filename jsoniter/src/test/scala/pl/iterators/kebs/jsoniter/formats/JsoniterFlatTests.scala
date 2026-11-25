package pl.iterators.kebs.jsoniter.formats

import com.github.plokhotnyuk.jsoniter_scala.core._
import pl.iterators.kebs.core.macros.{CaseClass1ToValueClass, ValueClassLike}
import pl.iterators.kebs.instances.TimeInstances
import pl.iterators.kebs.instances.time.mixins.InstantEpochMilliLong
import pl.iterators.kebs.jsoniter.KebsJsoniter
import pl.iterators.kebs.tagged._
import pl.iterators.kebs.testkit.model._
import pl.iterators.kebs.testkit.nested.{FlatChildren, FlatTree, Item, Wrapper}

import java.time.Instant

object JsoniterFlatTestDomain {
  final case class Wrapped(value: Int) extends AnyVal
  final case class Holder(wrapped: Wrapped, id: Id, n: Int)
  final case class Generic[A](value: A)
  final case class Tags(value: List[Item])
  final case class Maybe(value: Option[String])
  final case class Positive(value: Int) { require(value > 0, "not positive") }
  final case class HasPositive(p: Positive, n: Int)
  final case class FlatNode(edges: Map[String, FlatNode])
  final case class ByKey(ids: Map[Id, Int], wrapped: Map[Wrapped, Int])
  final case class Schedule(at: Map[Instant, Int])

  trait NameTag
  type Name = String @@ NameTag
  implicit val nameRep: ValueClassLike[Name, String] = new ValueClassLike[Name, String](_.taggedWith[NameTag], name => name)
  final case class Named(name: Name, others: List[Name])
}

class JsoniterFlatTests extends JsoniterTestBase {
  import JsoniterFlatTestDomain._

  object Default extends KebsJsoniter
  object Flat    extends KebsJsoniter with CaseClass1ToValueClass
  object Time    extends KebsJsoniter with TimeInstances
  object Milli   extends KebsJsoniter with InstantEpochMilliLong

  test("a value class is written as its field") {
    import Flat._
    roundTrip(Id("x"), "\"x\"")
    roundTrip(Wrapped(1), "1")
    roundTrip(Holder(Wrapped(1), Id("x"), 2), """{"wrapped":1,"id":"x","n":2}""")
    roundTrip(Person(Id("x"), "Ann", 30, None), """{"id":"x","name":"Ann","age":30,"nickname":null}""")
  }

  test("an object without CaseClass1ToValueClass, AnyVal or not") {
    import Default._
    roundTrip(Id("x"), """{"value":"x"}""")
    roundTrip(Wrapped(1), """{"value":1}""")
  }

  test("a value class of anything that has a codec") {
    import Flat._
    roundTrip(Generic(1), "1")
    roundTrip(Generic(List(Id("x"))), "[\"x\"]")
    roundTrip(Generic(Generic(true)), "true")
    roundTrip(Tags(List(Item("i", 1))), """[{"name":"i","n":1}]""")
    roundTrip(Maybe(Some("a")), "\"a\"")
    roundTrip(Maybe(None), "null")
    roundTrip(Wrapper(Item("i", 1)), """{"name":"i","n":1}""")
    roundTrip(FlatTree(1, FlatChildren(List(FlatTree(2, FlatChildren(Nil))))), """{"value":1,"children":[{"value":2,"children":[]}]}""")
  }

  test("tagged types") {
    import Default._
    val name = "x".taggedWith[NameTag]
    roundTrip(Named(name, List(name)), """{"name":"x","others":["x"]}""")
    roundTrip(name, "\"x\"")
  }

  test("a value class of itself") {
    import Flat._
    roundTrip(FlatNode(Map("a" -> FlatNode(Map.empty))), """{"a":{}}""")
  }

  test("a value that is rejected") {
    import Flat._
    rejected[Positive]("0", "not positive")
    rejected[HasPositive]("""{"p":0,"n":1}""", "not positive")
    rejected[Positive]("null", "")
  }

  test("as map keys") {
    import Flat._
    roundTrip(ByKey(Map(Id("x") -> 1), Map(Wrapped(2) -> 3)), """{"ids":{"x":1},"wrapped":{"2":3}}""")
    roundTrip(Map(Id("x") -> Id("y")), """{"x":"y"}""")
    rejected[Map[Positive, Int]]("""{"0":1}""", "not positive")
  }

  test("InstanceConverter") {
    val at = Instant.parse("2026-01-02T03:04:05Z")

    {
      import Time._
      roundTrip(at, "\"2026-01-02T03:04:05Z\"")
      roundTrip(Event(Id("x"), at), """{"id":{"value":"x"},"at":"2026-01-02T03:04:05Z"}""")
      rejected[Instant]("\"x\"", "cannot be parsed")
    }

    {
      import Milli._
      roundTrip(at, at.toEpochMilli.toString)
      roundTrip(Event(Id("x"), at), s"""{"id":{"value":"x"},"at":${at.toEpochMilli}}""")
      roundTrip(List(Option(at)), s"[${at.toEpochMilli}]")
      roundTrip(Schedule(Map(at -> 1)), s"""{"at":{"${at.toEpochMilli}":1}}""")
    }

    {
      import Default._
      roundTrip(Event(Id("x"), at), """{"id":{"value":"x"},"at":"2026-01-02T03:04:05Z"}""")
    }
  }

  test("explicit derivation of a value class") {
    roundTrip(Id("x"), """{"value":"x"}""")(Default.deriveCodec[Id])

    import Flat._
    roundTrip(Id("x"), "\"x\"")(deriveCodec[Id])
    roundTrip(Option(Id("x")), "\"x\"")(deriveCodec[Option[Id]])
  }
}
