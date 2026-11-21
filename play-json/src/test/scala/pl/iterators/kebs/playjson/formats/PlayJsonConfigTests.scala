package pl.iterators.kebs.playjson.formats

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.json.{JsonConfig, NoneHandling}
import pl.iterators.kebs.core.naming.{Naming => NamingStyle}
import pl.iterators.kebs.playjson.KebsPlayJson
import pl.iterators.kebs.playjson.annotations._
import pl.iterators.kebs.testkit.model._
import play.api.libs.json._

object PlayJsonConfigTestDomain {
  final case class WithDefault(a: Int, b: String = "x")
  final case class User(@fieldName("user_name") name: String, age: Int)
  final case class Session(id: String, @transientField cache: List[String] = Nil)
  final case class Point(x: Int, y: Int)
  object Point {
    implicit val format: Format[Point] = Format(
      Reads.StringReads.map { s =>
        val Array(x, y) = s.split(',')
        Point(x.toInt, y.toInt)
      },
      Writes.StringWrites.contramap(p => s"${p.x},${p.y}")
    )
  }
  final case class Located(name: String, at: Point)
  final case class Comp2(someField: Int)
  object Comp2 {
    implicit val format: OFormat[Comp2] = Json.format[Comp2]
  }
  final case class Tag(name: String, x: Int)
  final case class Tagged(tags: List[Tag], first: Option[Tag])
}

class PlayJsonConfigTests extends AnyFunSuite with Matchers {
  import PlayJsonConfigTestDomain._

  object Default extends KebsPlayJson
  object Kebab   extends KebsPlayJson {
    override def kebsJsonConfig: JsonConfig = super.kebsJsonConfig.withFieldNaming(NamingStyle.kebabCase)
  }
  object WithDefaults extends KebsPlayJson {
    override def kebsJsonConfig: JsonConfig = super.kebsJsonConfig.withDefaults
  }
  object WriteNulls extends KebsPlayJson {
    override def kebsJsonConfig: JsonConfig = super.kebsJsonConfig.withNoneHandling(NoneHandling.WriteNull)
  }

  test("derived reads and writes: None omitted, missing reads as None") {
    import Default._
    val person = Person(Id("x"), "Ann", 30, None)
    Json.toJson(person) shouldBe Json.parse("""{"id":{"value":"x"},"name":"Ann","age":30}""")
    Json.parse("""{"id":{"value":"x"},"name":"Ann","age":30}""").validate[Person] shouldBe JsSuccess(person)
    Json.parse("""{"id":{"value":"x"},"name":"Ann","age":30,"nickname":null}""").validate[Person] shouldBe JsSuccess(person)
  }

  test("field naming from JsonConfig") {
    import Kebab._
    Json.toJson(Naming(1, "a", 2, "u")) shouldBe
    Json.parse("""{"some-field":1,"another-field-name":"a","field1-name":2,"http-url":"u"}""")
  }

  test("snakified preset") {
    import pl.iterators.kebs.playjson.snakified._
    Json.toJson(Naming(1, "a", 2, "u")) shouldBe
    Json.parse("""{"some_field":1,"another_field_name":"a","field1_name":2,"http_url":"u"}""")
  }

  test("defaults from JsonConfig") {
    import WithDefaults._
    Json.parse("""{"a":1}""").validate[WithDefault] shouldBe JsSuccess(WithDefault(1, "x"))
  }

  test("field annotations") {
    import Default._
    Json.toJson(User("ann", 3)) shouldBe Json.parse("""{"user_name":"ann","age":3}""")
    Json.toJson(Session("s", List("c"))) shouldBe Json.parse("""{"id":"s"}""")
    Json.parse("""{"id":"s","cache":["c"]}""").validate[Session] shouldBe JsSuccess(Session("s", Nil))
  }

  test("NoneHandling.WriteNull") {
    import WriteNulls._
    Json.toJson(Person(Id("x"), "Ann", 30, None)) shouldBe
    Json.parse("""{"id":{"value":"x"},"name":"Ann","age":30,"nickname":null}""")
  }

  test("every field error is reported, with play's paths") {
    import Default._
    val errors = Json.parse("""{"at":5}""").validate[Located] match {
      case JsError(errors) => errors.map(_._1.toString).toSet
      case other           => fail(s"expected an error, got $other")
    }
    errors shouldBe Set("/name", "/at")
  }

  test("an existing instance wins, as a field and at the root") {
    import Default._
    Json.toJson(Located("n", Point(1, 2))) shouldBe Json.parse("""{"name":"n","at":"1,2"}""")
    Json.parse("""{"name":"n","at":"1,2"}""").validate[Located] shouldBe JsSuccess(Located("n", Point(1, 2)))
    Json.toJson(Point(3, 4)) shouldBe JsString("3,4")
  }

  test("an explicit derivation call always derives, with that protocol's config") {
    import Default._
    Kebab.deriveFormat[Comp2].writes(Comp2(1)) shouldBe Json.parse("""{"some-field":1}""")
    Json.toJson(Comp2(1)) shouldBe Json.parse("""{"someField":1}""")
  }

  test("a user's instance of a container field wins") {
    import Default._
    implicit val tagsFormat: Format[List[Tag]] = Format(
      Reads.StringReads.map(_.split(',').toList.map { tag =>
        val Array(name, x) = tag.split(':')
        Tag(name, x.toInt)
      }),
      Writes.StringWrites.contramap(_.map(t => s"${t.name}:${t.x}").mkString(","))
    )
    val tagged = Tagged(List(Tag("a", 1)), Some(Tag("b", 2)))
    val json   = Json.parse("""{"tags":"a:1","first":{"name":"b","x":2}}""")
    Json.toJson(tagged) shouldBe json
    json.validate[Tagged] shouldBe JsSuccess(tagged)
  }
}
