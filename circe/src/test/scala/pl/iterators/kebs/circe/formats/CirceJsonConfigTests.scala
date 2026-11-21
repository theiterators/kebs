package pl.iterators.kebs.circe.formats

import io.circe.{Decoder, Encoder, Json}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.circe.KebsCirce
import pl.iterators.kebs.circe.annotations._
import pl.iterators.kebs.core.json.{JsonConfig, NoneHandling}
import pl.iterators.kebs.core.naming.{Naming => NamingStyle}
import pl.iterators.kebs.testkit.model._

object CirceJsonConfigTestDomain {
  final case class WithDefault(a: Int, b: String = "x")
  final case class User(@fieldName("user_name") name: String, age: Int)
  final case class Point(x: Int, y: Int)
  object Point {
    implicit val encoder: Encoder[Point] = Encoder.encodeString.contramap(p => s"${p.x},${p.y}")
  }
  final case class Located(name: String, at: Point)
  final case class Tree(value: Int, children: List[Tree])
}

class CirceJsonConfigTests extends AnyFunSuite with Matchers {
  import CirceJsonConfigTestDomain._

  private def json(s: String): Json = io.circe.parser.parse(s).fold(throw _, identity)

  object Default extends KebsCirce
  object Kebab   extends KebsCirce {
    override implicit def kebsJsonConfig: JsonConfig = JsonConfig.default.withFieldNaming(NamingStyle.kebabCase)
  }
  object Discriminated extends KebsCirce {
    override implicit def kebsJsonConfig: JsonConfig = JsonConfig.default.withDiscriminator("type")
  }
  object SnakeConstructors extends KebsCirce {
    override implicit def kebsJsonConfig: JsonConfig = JsonConfig.default.withConstructorNaming(NamingStyle.snakeCase)
  }
  object WithDefaults extends KebsCirce {
    override implicit def kebsJsonConfig: JsonConfig = JsonConfig.default.withDefaults
  }
  object Omitting extends KebsCirce {
    override implicit def kebsJsonConfig: JsonConfig = JsonConfig.default.withNoneHandling(NoneHandling.Omit)
  }

  test("field naming from JsonConfig") {
    import Kebab._
    implicitly[Encoder[Naming]].apply(Naming(1, "a", 2, "u")) shouldBe json(
      """{"some-field":1,"another-field-name":"a","field1-name":2,"http-url":"u"}"""
    )
  }

  test("discriminator from JsonConfig") {
    import Discriminated._
    val circle: Shape = Circle(1.0, "c")
    implicitly[Encoder[Shape]].apply(circle) shouldBe json("""{"r":1.0,"label":"c","type":"Circle"}""")
    implicitly[Decoder[Shape]].decodeJson(json("""{"r":1.0,"label":"c","type":"Circle"}""")) shouldBe Right(circle)
  }

  test("constructor naming from JsonConfig") {
    import SnakeConstructors._
    implicitly[Encoder[Shape]].apply(Circle(1.0, "c")) shouldBe json("""{"circle":{"r":1.0,"label":"c"}}""")
  }

  test("defaults from JsonConfig") {
    import WithDefaults._
    implicitly[Decoder[WithDefault]].decodeJson(json("""{"a":1}""")) shouldBe Right(WithDefault(1, "x"))
  }

  test("field annotations") {
    import Default._
    implicitly[Encoder[User]].apply(User("ann", 3)) shouldBe json("""{"user_name":"ann","age":3}""")
  }

  test("an instance in the companion wins over derivation") {
    import Default._
    implicitly[Encoder[Located]].apply(Located("n", Point(1, 2))) shouldBe json("""{"name":"n","at":"1,2"}""")
  }

  test("recursive types need no workaround") {
    import Default._
    val tree = Tree(1, List(Tree(2, Nil)))
    implicitly[Decoder[Tree]].decodeJson(implicitly[Encoder[Tree]].apply(tree)) shouldBe Right(tree)
  }

  test("NoneHandling.Omit is rejected until kindlings supports it") {
    import Omitting._
    an[IllegalArgumentException] should be thrownBy implicitly[Encoder[Person]]
  }
}
