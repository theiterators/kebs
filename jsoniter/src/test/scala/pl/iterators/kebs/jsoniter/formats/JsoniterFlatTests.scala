package pl.iterators.kebs.jsoniter.formats

import com.github.plokhotnyuk.jsoniter_scala.core._
import pl.iterators.kebs.core.instances.InstanceConverter
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
  final case class Dashed(a: Option[Int], b: List[Option[Int]])
  final case class Owned(a: Int, b: Int)
  object Owned {
    implicit val codec: JsonValueCodec[Owned] =
      JsoniterNestedInstancesTestDomain.asString(text => Owned(text.take(1).toInt, text.drop(1).toInt), o => s"${o.a}${o.b}")
  }
  final case class HasOwned(owned: Owned, all: List[Owned])
  final case class Numbers(a: Int, b: List[Int], c: Map[Int, Int], d: Option[Int], e: String)

  // a user's instance with code a macro made while it was searched for, met in several containers of one type
  final case class Cents(value: Long)
  object Cents {
    implicit def viaValueClass(implicit rep: ValueClassLike[Cents, Long]): JsonValueCodec[Cents] =
      JsoniterNestedInstancesTestDomain.asString(text => rep.apply(text.toLong), cents => rep.unapply(cents).toString)
  }
  final case class Wallet(all: List[Cents], best: Option[Cents], pair: (Cents, Cents), byShop: Map[String, Cents])

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
  object Self    extends KebsJsoniter {
    implicit val trimmed: InstanceConverter[String, String] = InstanceConverter[String, String](_.trim, _.trim)
    implicit val shown: InstanceConverter[Int, Long]        = InstanceConverter[Int, Long](_.toLong, _.toInt)
    implicit val read: InstanceConverter[Long, Int]         = InstanceConverter[Long, Int](_.toInt, _.toLong)
  }
  object Dashes extends KebsJsoniter {
    implicit val dashes: InstanceConverter[Option[Int], String] =
      InstanceConverter[Option[Int], String](_.fold("-")(_.toString), text => if (text == "-") None else Some(text.toInt))
  }
  object Twice extends KebsJsoniter {
    implicit val first: InstanceConverter[Owned, String]  = InstanceConverter[Owned, String](_.a.toString, text => Owned(text.toInt, 0))
    implicit val second: InstanceConverter[Owned, String] = InstanceConverter[Owned, String](_.b.toString, text => Owned(0, text.toInt))
  }
  object Hex extends KebsJsoniter {
    implicit val hex: InstanceConverter[Int, String] = InstanceConverter[Int, String](_.toHexString, Integer.parseInt(_, 16))
  }

  test("a value class is written as its field") {
    import Flat._
    roundTrip(Id("x"), "\"x\"")
    roundTrip(Wrapped(1), "1")
    roundTrip(Holder(Wrapped(1), Id("x"), 2), """{"wrapped":1,"id":"x","n":2}""")
    roundTrip(Person(Id("x"), "Ann", 30, None), """{"id":"x","name":"Ann","age":30,"nickname":null}""")
  }

  test("a user's instance made by a macro, in several containers") {
    import Flat._
    roundTrip(
      Wallet(List(Cents(1)), Some(Cents(2)), (Cents(3), Cents(4)), Map("a" -> Cents(5))),
      """{"all":["1"],"best":"2","pair":["3","4"],"byShop":{"a":"5"}}"""
    )
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

  test("an InstanceConverter of a type that has a codec without one") {
    import Hex._
    roundTrip(Numbers(255, List(16), Map(32 -> 33), Some(1), "n"), """{"a":"ff","b":["10"],"c":{"20":"21"},"d":"1","e":"n"}""")
    roundTrip(255, "\"ff\"")
  }

  test("a type that is converted to itself is reported") {
    import Self._
    "deriveCodec[List[String]]" shouldNot compile
    "deriveCodec[Map[String, Boolean]]" shouldNot compile
    "deriveCodec[Tags]" shouldNot compile
    roundTrip(List(true), "[true]")(deriveCodec[List[Boolean]])
  }

  test("explicit derivation of a value class") {
    roundTrip(Id("x"), """{"value":"x"}""")(Default.deriveCodec[Id])

    import Flat._
    roundTrip(Id("x"), "\"x\"")(deriveCodec[Id])
    roundTrip(Option(Id("x")), "\"x\"")(deriveCodec[Option[Id]])
  }

  test("an InstanceConverter of an Option") {
    import Dashes._
    roundTrip(Dashed(None, List(Some(1), None)), """{"a":"-","b":["1","-"]}""")
  }

  test("a type's own codec, whatever converters there are") {
    import Twice._
    roundTrip(Owned(1, 2), "\"12\"")
    roundTrip(HasOwned(Owned(1, 2), List(Owned(3, 4))), """{"owned":"12","all":["34"]}""")
  }

  test("case classes of the standard library are not value classes") {
    import Flat._
    "implicitly[JsonValueCodec[Some[String]]]" shouldNot compile
    "implicitly[JsonValueCodec[Right[Int, String]]]" shouldNot compile
    "implicitly[JsonValueCodec[Tuple1[String]]]" shouldNot compile
    "implicitly[JsonValueCodec[scala.util.Success[Int]]]" shouldNot compile
    "deriveCodec[List[Some[String]]]" shouldNot compile
  }
}
