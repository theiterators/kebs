package pl.iterators.kebs.jsoniter.formats

import com.github.plokhotnyuk.jsoniter_scala.core._
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.enumeratum.KebsEnumeratum
import pl.iterators.kebs.jsoniter.KebsJsoniter
import pl.iterators.kebs.jsoniter.enums.KebsJsoniterEnums
import pl.iterators.kebs.testkit.model._
import pl.iterators.kebs.testkit.nested.{Gen, Item}

import scala.collection.mutable

object JsoniterImplicitTestDomain {
  case object Alone
}

class JsoniterImplicitTests extends JsoniterTestBase {
  import JsoniterImplicitTestDomain._

  object Protocol extends KebsJsoniter
  object Enums    extends KebsJsoniter with KebsEnumeratum with KebsJsoniterEnums
  object Flat     extends KebsJsoniter with CaseClass1ToValueClass

  private val item = Item("i", 1)

  test("case classes") {
    import Protocol._
    roundTrip(item, """{"name":"i","n":1}""")
    roundTrip(Gen(item, 2), """{"x":{"name":"i","n":1},"n":2}""")
    implicitly[JsonValueCodec[Person]] should not be null
  }

  test("collections and arrays") {
    import Protocol._
    roundTrip(List(item), """[{"name":"i","n":1}]""")
    roundTrip(Seq(item), """[{"name":"i","n":1}]""")
    roundTrip(Vector(Set(item)), """[[{"name":"i","n":1}]]""")
    roundTrip(mutable.Buffer(item), """[{"name":"i","n":1}]""")
    roundTrip(Map("a" -> item), """{"a":{"name":"i","n":1}}""")
    roundTrip(List(Option(item), None), """[{"name":"i","n":1},null]""")
    roundTrip(List(1, 2), "[1,2]")
    roundTrip(List((1, "a")), """[[1,"a"]]""")
    writeToString(Array(item)) shouldBe """[{"name":"i","n":1}]"""
    readFromString[Array[Item]]("""[{"name":"i","n":1}]""").toList shouldBe List(item)
  }

  test("no implicit codec of what other libraries handle, or what is ambiguous as JSON") {
    import Protocol._
    "implicitly[JsonValueCodec[String]]" shouldNot compile
    "implicitly[JsonValueCodec[Int]]" shouldNot compile
    "implicitly[JsonValueCodec[Option[Item]]]" shouldNot compile
    "implicitly[JsonValueCodec[(Int, Item)]]" shouldNot compile
    "implicitly[JsonValueCodec[Either[Int, Item]]]" shouldNot compile
    "implicitly[JsonValueCodec[Array[Byte]]]" shouldNot compile
    "implicitly[JsonValueCodec[Array[Char]]]" shouldNot compile
    "implicitly[JsonValueCodec[Unit]]" shouldNot compile
    "implicitly[JsonValueCodec[Any]]" shouldNot compile
    "implicitly[JsonValueCodec[Nothing]]" shouldNot compile
    "implicitly[JsonValueCodec[java.time.Instant]]" shouldNot compile
    "implicitly[JsonValueCodec[Some[Item]]]" shouldNot compile
    "implicitly[JsonValueCodec[List[Thread]]]" shouldNot compile
    "def f[A]: JsonValueCodec[A] = implicitly[JsonValueCodec[A]]" shouldNot compile
    "def f[A]: JsonValueCodec[List[A]] = implicitly[JsonValueCodec[List[A]]]" shouldNot compile
    "writeToString(Alone)" shouldNot compile
    "implicitly[JsonValueCodec[Item]]" should compile
    "implicitly[JsonValueCodec[List[String]]]" should compile
  }

  test("an enum's value at its own type") {
    import Enums._
    "writeToString(Color.DarkBlue)" shouldNot compile
    writeToString[Color](Color.DarkBlue) shouldBe "\"DarkBlue\""
  }

  test("a generic method with a codec") {
    import Protocol._
    def written[A: JsonValueCodec](a: A): String = writeToString(a)
    def gen[A: JsonValueCodec](a: A): String     = writeToString(Gen(a, 1))
    written(item) shouldBe """{"name":"i","n":1}"""
    gen(item) shouldBe """{"x":{"name":"i","n":1},"n":1}"""
  }

  test("an explicit derivation uses its protocol's config, and is the instance that is found") {
    import Protocol._
    implicit val codec: JsonValueCodec[Naming] = pl.iterators.kebs.jsoniter.snakified.deriveCodec[Naming]
    writeToString(List(Naming(1, "a", 2, "u"))) shouldBe """[{"some_field":1,"another_field_name":"a","field1_name":2,"http_url":"u"}]"""
  }

  test("through the package object") {
    import pl.iterators.kebs.jsoniter._
    roundTrip(item, """{"name":"i","n":1}""")
  }
}
