package pl.iterators.kebs.jsoniter.formats

import com.github.plokhotnyuk.jsoniter_scala.core._
import pl.iterators.kebs.core.json.{JsonConfig, NoneHandling}
import pl.iterators.kebs.core.naming.{Naming => NamingStyle}
import pl.iterators.kebs.jsoniter.KebsJsoniter
import pl.iterators.kebs.jsoniter.annotations._
import pl.iterators.kebs.testkit.model._

object JsoniterJsonConfigTestDomain {
  final case class WithDefault(a: Int, b: String = "x", c: Option[Int] = Some(3))
  final case class User(@fieldName("user_name") name: String, age: Int)
  final case class Awkward(@fieldName("ż\"é\\") a: Int, @fieldName("") b: Int)
  final case class Session(id: String, @transientField cache: List[String] = Nil)
  final case class InnerPart(someField: Int)
  final case class OuterPart(innerPart: InnerPart, otherField: String)
}

class JsoniterJsonConfigTests extends JsoniterTestBase {
  import JsoniterJsonConfigTestDomain._

  object Default extends KebsJsoniter
  object Kebab   extends KebsJsoniter {
    override def kebsJsonConfig: JsonConfig = super.kebsJsonConfig.withFieldNaming(NamingStyle.kebabCase)
  }
  object WithDefaults extends KebsJsoniter {
    override def kebsJsonConfig: JsonConfig = super.kebsJsonConfig.withDefaults
  }
  object Omitting extends KebsJsoniter {
    override def kebsJsonConfig: JsonConfig = super.kebsJsonConfig.withNoneHandling(NoneHandling.Omit)
  }

  test("field naming") {
    roundTrip(Naming(1, "a", 2, "u"), """{"some-field":1,"another-field-name":"a","field1-name":2,"http-url":"u"}""")(
      Kebab.deriveCodec[Naming]
    )
  }

  test("field naming applies to nested types") {
    roundTrip(OuterPart(InnerPart(1), "o"), """{"inner-part":{"some-field":1},"other-field":"o"}""")(Kebab.deriveCodec[OuterPart])
  }

  test("presets") {
    import pl.iterators.kebs.jsoniter.{capitalized, snakified}
    roundTrip(Naming(1, "a", 2, "u"), """{"some_field":1,"another_field_name":"a","field1_name":2,"http_url":"u"}""")(
      snakified.deriveCodec[Naming]
    )
    roundTrip(Naming(1, "a", 2, "u"), """{"SomeField":1,"AnotherFieldName":"a","Field1Name":2,"HttpURL":"u"}""")(
      capitalized.deriveCodec[Naming]
    )
  }

  test("NoneHandling.Omit") {
    roundTrip(Person(Id("x"), "Ann", 30, None), """{"id":{"value":"x"},"name":"Ann","age":30}""")(Omitting.deriveCodec[Person])
    readFromString[Person]("""{"id":{"value":"x"},"name":"Ann","age":30,"nickname":null}""")(
      Omitting.deriveCodec[Person]
    ).nickname shouldBe None
  }

  test("defaults only when JsonConfig says so") {
    rejected[WithDefault]("""{"a":1}""", "b")(Default.deriveCodec[WithDefault])
    readFromString[WithDefault]("""{"a":1}""")(WithDefaults.deriveCodec[WithDefault]) shouldBe WithDefault(1, "x", Some(3))
    readFromString[WithDefault]("""{"a":1,"c":null}""")(WithDefaults.deriveCodec[WithDefault]) shouldBe WithDefault(1, "x", None)
    readFromString[WithDefault]("""{"a":1,"b":"y"}""")(Default.deriveCodec[WithDefault]) shouldBe WithDefault(1, "y", None)
  }

  test("field annotations") {
    roundTrip(User("ann", 3), """{"user_name":"ann","age":3}""")(Kebab.deriveCodec[User])
    roundTrip(Awkward(1, 2), """{"ż\"é\\":1,"":2}""")(Default.deriveCodec[Awkward])
    writeToString(Session("s", List("c")))(Default.deriveCodec[Session]) shouldBe """{"id":"s"}"""
    readFromString[Session]("""{"id":"s","cache":["c"]}""")(Default.deriveCodec[Session]) shouldBe Session("s", Nil)
  }

  test("the protocol's config, whatever JsonConfig is implicitly in scope") {
    implicit val unrelated: JsonConfig = JsonConfig.default.withFieldNaming(NamingStyle.uppercase)
    writeToString(InnerPart(1))(Kebab.deriveCodec[InnerPart]) shouldBe """{"some-field":1}"""
    unrelated.useDefaults shouldBe false
  }
}
