package pl.iterators.kebs.sprayjson.formats

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.json.{JsonConfig, NoneHandling}
import pl.iterators.kebs.core.naming.{Naming => NamingStyle}
import pl.iterators.kebs.sprayjson.KebsSprayJson
import pl.iterators.kebs.sprayjson.annotations._
import pl.iterators.kebs.testkit.model._
import spray.json._

object SprayJsonConfigTestDomain {
  final case class WithDefault(a: Int, b: String = "x")
  final case class User(@fieldName("user_name") name: String, age: Int)
  final case class Session(id: String, @transientField cache: List[String] = Nil)
  final case class Point(x: Int, y: Int)
  object Point {
    implicit val format: JsonFormat[Point] = new JsonFormat[Point] {
      def write(p: Point): JsValue   = JsString(s"${p.x},${p.y}")
      def read(json: JsValue): Point = json match {
        case JsString(s) =>
          val Array(x, y) = s.split(',')
          Point(x.toInt, y.toInt)
        case other => deserializationError(s"Expected point, got $other")
      }
    }
  }
  final case class Located(name: String, at: Point)
  final case class Inner(a: Int)
  final case class Outer(inner: Inner, n: Int)
  final case class Comp2(someField: Int)
  object Comp2 {
    import DefaultJsonProtocol._
    implicit val format: RootJsonFormat[Comp2] = jsonFormat1(Comp2.apply)
  }
  final case class Tag(name: String, x: Int)
  final case class Tagged(tags: List[Tag], first: Option[Tag])
  final case class Rng private (from: Int, to: Int)
  object Rng {
    def of(from: Int, to: Int): Rng = new Rng(from, to)
  }
  final case class MultiList(a: Int)(val b: String)
}

class SprayJsonConfigTests extends AnyFunSuite with Matchers {
  import SprayJsonConfigTestDomain._

  object Default extends DefaultJsonProtocol with KebsSprayJson
  object Kebab   extends DefaultJsonProtocol with KebsSprayJson {
    override def kebsJsonConfig: JsonConfig = super.kebsJsonConfig.withFieldNaming(NamingStyle.kebabCase)
  }
  object WithDefaults extends DefaultJsonProtocol with KebsSprayJson {
    override def kebsJsonConfig: JsonConfig = super.kebsJsonConfig.withDefaults
  }
  object WriteNulls extends DefaultJsonProtocol with KebsSprayJson {
    override def kebsJsonConfig: JsonConfig = super.kebsJsonConfig.withNoneHandling(NoneHandling.WriteNull)
  }

  test("field naming from JsonConfig") {
    import Kebab._
    implicitly[RootJsonFormat[Naming]].write(Naming(1, "a", 2, "u")) shouldBe
    """{"some-field":1,"another-field-name":"a","field1-name":2,"http-url":"u"}""".parseJson
  }

  test("defaults from JsonConfig") {
    import WithDefaults._
    implicitly[RootJsonFormat[WithDefault]].read("""{"a":1}""".parseJson) shouldBe WithDefault(1, "x")
  }

  test("field annotations") {
    import Default._
    implicitly[RootJsonFormat[User]].write(User("ann", 3)) shouldBe """{"user_name":"ann","age":3}""".parseJson
    val session = implicitly[RootJsonFormat[Session]]
    session.write(Session("s", List("c"))) shouldBe """{"id":"s"}""".parseJson
    session.read("""{"id":"s","cache":["c"]}""".parseJson) shouldBe Session("s", Nil)
  }

  test("an existing instance wins, as a field and at the root") {
    import Default._
    implicitly[RootJsonFormat[Located]].write(Located("n", Point(1, 2))) shouldBe """{"name":"n","at":"1,2"}""".parseJson
    implicitly[RootJsonFormat[Located]].read("""{"name":"n","at":"1,2"}""".parseJson) shouldBe Located("n", Point(1, 2))
    implicitly[RootJsonFormat[Point]].write(Point(1, 2)) shouldBe JsString("1,2")
  }

  test("NoneHandling.WriteNull without NullOptions") {
    import WriteNulls._
    implicitly[RootJsonFormat[Person]].write(Person(Id("x"), "Ann", 30, None)) shouldBe
    """{"id":{"value":"x"},"name":"Ann","age":30,"nickname":null}""".parseJson
  }

  test("another protocol's derivation uses that protocol's config") {
    import Default._
    val format = Kebab.deriveFormat[Naming]
    format.write(Naming(1, "a", 2, "u")) shouldBe
    """{"some-field":1,"another-field-name":"a","field1-name":2,"http-url":"u"}""".parseJson
  }

  test("an explicit derivation call always derives, with that protocol's config") {
    import Default._
    Kebab.deriveFormat[Comp2].write(Comp2(1)) shouldBe """{"some-field":1}""".parseJson
    implicitly[RootJsonFormat[Comp2]].write(Comp2(1)) shouldBe """{"someField":1}""".parseJson
  }

  test("a derived format handed to an implicit val is not that val") {
    object Holder {
      import Default._
      private def derived: RootJsonFormat[Inner] = deriveFormat[Inner]
      implicit val format: RootJsonFormat[Inner] = derived
    }
    Holder.format.write(Inner(1)) shouldBe """{"a":1}""".parseJson
  }

  test("a user's format of a container field wins") {
    import Default._
    implicit val tagsFormat: JsonFormat[List[Tag]] = new JsonFormat[List[Tag]] {
      def write(tags: List[Tag]): JsValue = JsString(tags.map(t => s"${t.name}:${t.x}").mkString(","))
      def read(json: JsValue): List[Tag]  = json match {
        case JsString(s) =>
          s.split(',').toList.map { tag =>
            val Array(name, x) = tag.split(':')
            Tag(name, x.toInt)
          }
        case other => deserializationError(s"Expected tags, got $other")
      }
    }
    val tagged = Tagged(List(Tag("a", 1)), Some(Tag("b", 2)))
    val json   = """{"tags":"a:1","first":{"name":"b","x":2}}""".parseJson
    implicitly[RootJsonFormat[Tagged]].write(tagged) shouldBe json
    implicitly[RootJsonFormat[Tagged]].read(json) shouldBe tagged
  }

  test("a case class with a private constructor") {
    import Default._
    implicitly[RootJsonFormat[Rng]].write(Rng.of(1, 2)) shouldBe """{"from":1,"to":2}""".parseJson
    implicitly[RootJsonFormat[Rng]].read("""{"from":1,"to":2}""".parseJson) shouldBe Rng.of(1, 2)
  }

  test("a case class with more than one parameter list is rejected") {
    import Default._
    "implicitly[RootJsonFormat[MultiList]]" shouldNot typeCheck
  }

  test("a protocol mixed into a class") {
    class Routes extends DefaultJsonProtocol with KebsSprayJson {
      def located: RootJsonFormat[Located] = implicitly[RootJsonFormat[Located]]
    }
    new Routes().located.write(Located("n", Point(1, 2))) shouldBe """{"name":"n","at":"1,2"}""".parseJson
  }

  test("missing and malformed fields are reported like spray's fromField") {
    import Default._
    val missing = intercept[DeserializationException](implicitly[RootJsonFormat[Located]].read("""{"name":"n"}""".parseJson))
    missing.msg shouldBe "Object is missing required member 'at'"
    missing.fieldNames shouldBe List("at")
    val nested = intercept[DeserializationException](implicitly[RootJsonFormat[Outer]].read("""{"inner":{"a":"x"},"n":1}""".parseJson))
    nested.fieldNames shouldBe List("inner", "a")
  }
}
