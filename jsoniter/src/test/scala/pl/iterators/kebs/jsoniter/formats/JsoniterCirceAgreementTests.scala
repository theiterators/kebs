package pl.iterators.kebs.jsoniter.formats

import com.github.plokhotnyuk.jsoniter_scala.core._
import io.circe.{Decoder, Encoder}
import org.scalatest.Assertion
import pl.iterators.kebs.circe.KebsCirce
import pl.iterators.kebs.circe.enums.{KebsCirceEnums, KebsCirceValueEnums}
import pl.iterators.kebs.core.json.{JsonConfig, NoneHandling}
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.core.naming.{Naming => NamingStyle}
import pl.iterators.kebs.enumeratum.{KebsEnumeratum, KebsValueEnumeratum}
import pl.iterators.kebs.jsoniter.KebsJsoniter
import pl.iterators.kebs.jsoniter.enums.{KebsJsoniterEnums, KebsJsoniterValueEnums}
import pl.iterators.kebs.testkit.model._
import pl.iterators.kebs.testkit.nested._
import pl.iterators.kebs.testkit.{model, nested}

object JsoniterCirceAgreementTestDomain {
  final case class Numbers(a: Byte, b: Short, c: Int, d: Long, e: Float, f: Double, g: BigInt, h: BigDecimal, i: Boolean, j: Char)
  final case class WithDefaults(a: Int, b: String = "b", c: Option[Int] = Some(1))

  abstract class Pair(config: JsonConfig) {
    object Jsoniter extends KebsJsoniter { override def kebsJsonConfig: JsonConfig = config }
    object Circe    extends KebsCirce    { override def kebsJsonConfig: JsonConfig = config }
  }
  object Default  extends Pair(JsonConfig.default)
  object Snake    extends Pair(JsonConfig.default.withFieldNaming(NamingStyle.snakeCase).withConstructorNaming(NamingStyle.kebabCase))
  object Omit     extends Pair(JsonConfig.default.withNoneHandling(NoneHandling.Omit))
  object Defaults extends Pair(JsonConfig.default.withDefaults)
  object Typed    extends Pair(JsonConfig.default.withDiscriminator("$type"))
  object Flat     extends CaseClass1ToValueClass
  object Enums    extends KebsEnumeratum with KebsValueEnumeratum
  object Enum1    extends KebsJsoniter with KebsJsoniterEnums with KebsJsoniterValueEnums
  object Enum2    extends KebsCirce with KebsCirceEnums with KebsCirceValueEnums
}

abstract class JsoniterCirceTestBase extends JsoniterTestBase {
  protected def agree[T](value: T)(implicit codec: JsonValueCodec[T], encoder: Encoder[T], decoder: Decoder[T]): Assertion = {
    val json = encoder(value).noSpaces
    writeToString(value) shouldBe json
    readFromString[T](json) shouldBe value
    decoder.decodeJson(encoder(value)) shouldBe Right(value)
  }
}

// both modules write the same JSON and read each other's
class JsoniterCirceAgreementTests extends JsoniterCirceTestBase {
  import JsoniterCirceAgreementTestDomain._

  private def agreeUpToOrder[T](value: T)(implicit codec: JsonValueCodec[T], encoder: Encoder[T]): Assertion = {
    io.circe.parser.parse(writeToString(value)) shouldBe Right(encoder(value))
    readFromString[T](encoder(value).noSpaces) shouldBe value
  }

  private val person  = Person(Id("x"), "Ann", 30, None)
  private val numbers = Numbers(-1, 2, 3, Long.MaxValue, 1.25f, -0.1, BigInt(2).pow(70), BigDecimal("12.30"), i = true, 'ż')
  private val shapes  = List[Shape](Circle(1.5, "c"), model.Empty)

  test("case classes") {
    import Default.Jsoniter._, Default.Circe._
    agree(person)
    agree(person.copy(nickname = Some("\"ó\n\u0001")))
    agree(numbers)
    agree(Wide.sample)
    agree(model.Naming(1, "a", 2, "u"))
    agree(Paint(Color.DarkBlue, Level.High))
    agree(HasList(List(item), 1))
    agree(HasOption(None, 1))
    agree(HasMap(Map("k" -> item), 1))
    agree(HasTuple((1, item), 1))
    agree(HasPages(Page(List(Page(List(item)))), 1))
    agree(HasGen(Gen(item, 2), 1))
    agree(Diamond0(Diamond1(item, item), Diamond1(item, item)))
    agree(List(Option(item), None))
    agree(Map("k" -> Vector(Set(item))))
  }

  test("sealed hierarchies") {
    import Default.Jsoniter._, Default.Circe._
    agree(shapes)
    agree(adt)
    agree(HasAdt(adt, 1))
  }

  test("recursive types") {
    import Default.Jsoniter._, Default.Circe._
    agree(RecOption(1, Some(RecOption(2, None))))
    agree(RecMap(1, Map("k" -> RecMap(2, Map.empty))))
    agree(RecTuple(1, List("k" -> RecTuple(2, Nil))))
    agree(recPage)
    agree(mutA)
    agree(nested.RecOptionPage(1, Some(Page(List(nested.RecOptionPage(2, None))))))
  }

  test("naming") {
    import Snake.Jsoniter._, Snake.Circe._
    agree(model.Naming(1, "a", 2, "u"))
    agree(shapes)
    agree(adt)
  }

  test("NoneHandling.Omit") {
    import Omit.Jsoniter._, Omit.Circe._
    agree(person)
    agree(mutA)
    agree(HasOption(None, 1))
    agree(List(Option(1), None))
  }

  test("defaults") {
    import Defaults.Jsoniter._, Defaults.Circe._
    agree(WithDefaults(1))
    List("""{"a":1}""", """{"a":1,"c":null}""", """{"a":1,"b":"x"}""", """{"a":1,"c":2,"z":{}}""").foreach { json =>
      Right(readFromString[WithDefaults](json)) shouldBe io.circe.parser.decode[WithDefaults](json)
    }
  }

  test("discriminator (circe writes it last)") {
    import Typed.Jsoniter._, Typed.Circe._
    agreeUpToOrder(shapes)
    agreeUpToOrder(adt)
    agreeUpToOrder(HasAdt(adt, 1))
  }

  test("value classes") {
    import Default.Jsoniter._, Default.Circe._, Flat._
    agree(person)
    agree(HasWrapper(Wrapper(item), 1))
    agree(FlatTree(1, FlatChildren(List(FlatTree(2, FlatChildren(Nil))))))
    agree(List(Id("x")))
  }

  test("enums") {
    import Enum1._, Enum2._, Enums._
    agree(Paint(Color.DarkBlue, Level.High))
  }

  test("what is rejected by both") {
    import Default.Jsoniter._, Default.Circe._
    List(
      """{"id":{"value":"x"},"name":"Ann"}""",
      """{"id":{"value":"x"},"name":null,"age":30}""",
      """{"id":"x","name":"Ann","age":30}""",
      """{"id":{"value":"x"},"name":"Ann","age":"x"}""",
      "[]",
      "null"
    ).foreach { json =>
      io.circe.parser.decode[Person](json).isLeft shouldBe true
      rejected[Person](json, "")
    }
  }
}

// with the other tests, the class would be too large
class JsoniterCirceLargeAgreementTests extends JsoniterCirceTestBase {
  import JsoniterCirceAgreementTestDomain._
  import pl.iterators.kebs.testkit.large._

  test("large sealed hierarchies") {
    import Default.Jsoniter._, Default.Circe._
    agree(List[Large](C1(1, X1(2)), C150(3, X150(4)), O120))
  }
}
