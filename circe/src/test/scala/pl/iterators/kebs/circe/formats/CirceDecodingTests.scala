package pl.iterators.kebs.circe.formats

import io.circe.{Decoder, Encoder, Json}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.circe.KebsCirce
import pl.iterators.kebs.circe.annotations._
import pl.iterators.kebs.core.json.{JsonConfig, NoneHandling}

object CirceDecodingTestDomain {
  final case class Inner(a: Int, b: String)
  final case class Outer(i: Inner, n: Int, o: Option[Int])
  final case class AllOptional(a: Option[Int], b: Option[String])
  final case class NoFields()
  sealed trait Sum
  final case class Circle(r: Int, l: String) extends Sum
  final case class Square(s: Int)            extends Sum
  case object Empty                          extends Sum
  final case class HasSum(s: Sum, l: List[Inner])
  final case class Defaults(a: Int, b: Int = 5, o: Option[Int] = Some(6), @transientField t: String = "t")
  final case class Optional(a: Int, o: Option[Int], s: Some[Int])
  final case class CustomOption(o: Option[Boolean])
}

// paths and messages are those kebs 2.3.0 reported on Scala 2 (circe-generic's), unless noted
class CirceDecodingTests extends AnyFunSuite with Matchers {
  import CirceDecodingTestDomain._

  object Default       extends KebsCirce
  object Discriminated extends KebsCirce {
    override def kebsJsonConfig: JsonConfig = JsonConfig.default.withDiscriminator("type")
  }
  object WithDefaults extends KebsCirce {
    override def kebsJsonConfig: JsonConfig = JsonConfig.default.withDefaults
  }
  object Omitting extends KebsCirce {
    override def kebsJsonConfig: JsonConfig = JsonConfig.default.withNoneHandling(NoneHandling.Omit)
  }

  private def json(s: String): Json = io.circe.parser.parse(s).fold(throw _, identity)

  private def error[T](s: String)(implicit decoder: Decoder[T]): String =
    decoder.decodeJson(json(s)).left.map(_.getMessage).left.getOrElse("decoded")

  private def errors[T](s: String)(implicit decoder: Decoder[T]): List[String] =
    decoder.decodeAccumulating(json(s).hcursor).fold(_.toList.map(_.getMessage), _ => Nil)

  test("the first error, with its path") {
    import Default._
    error[Outer]("""{"i":{"a":1},"n":1}""") shouldBe "DecodingFailure at .i.b: Missing required field"
    error[Outer]("""{"i":{"a":"x","b":2},"n":"y"}""") shouldBe "DecodingFailure at .i.a: Int"
    error[List[Inner]]("""[{"a":1,"b":"x"},{"a":"q"}]""") shouldBe "DecodingFailure at [1].a: Int"
  }

  test("accumulating decoding reports every field") {
    import Default._
    errors[Outer]("""{"i":{"a":"x","b":2},"n":"y","o":"z"}""") shouldBe List(
      "DecodingFailure at .i.a: Int",
      "DecodingFailure at .i.b: Got value '2' with wrong type, expecting string",
      "DecodingFailure at .n: Int",
      "DecodingFailure at .o: Int"
    )
    errors[HasSum]("""{"s":{"Circle":{"r":"q"}},"l":[{"a":"z"}]}""") shouldBe List(
      "DecodingFailure at .s.Circle.r: Int",
      "DecodingFailure at .s.Circle.l: Missing required field",
      "DecodingFailure at .l[0].a: Int",
      "DecodingFailure at .l[0].b: Missing required field"
    )
    errors[Outer]("""{"i":{"a":1,"b":"b"},"n":1}""") shouldBe Nil
  }

  test("input that is not an object") {
    import Default._
    error[Outer]("5") shouldBe "DecodingFailure at .i: Missing required field"
    error[AllOptional]("5") shouldBe "DecodingFailure at .a: Missing required field"
    error[AllOptional]("[]") shouldBe "DecodingFailure at .a: Missing required field"
    error[NoFields]("5") shouldBe "DecodingFailure at : Got value '5' with wrong type, expecting object"
    error[Sum]("""{"Empty":5}""") shouldBe "DecodingFailure at .Empty: Got value '5' with wrong type, expecting object"
    Decoder[AllOptional].decodeJson(json("{}")) shouldBe Right(AllOptional(None, None))
    Decoder[NoFields].decodeJson(json("{}")) shouldBe Right(NoFields())
  }

  test("sealed hierarchies") {
    import Default._
    error[Sum]("""{"Circle":{"r":"q"}}""") shouldBe "DecodingFailure at .Circle.r: Int"
    Decoder[Sum].decodeJson(json("""{"Square":{"s":1},"Circle":{"r":1,"l":"x"}}""")) shouldBe Right(Circle(1, "x"))
    // kebs 2.3.0: "JSON decoding to CNil should never happen"
    error[Sum]("""{"Nope":{}}""") shouldBe "DecodingFailure at : Unknown case 'Nope', expected one of: Circle, Square, Empty"
    error[Sum]("5") shouldBe "DecodingFailure at : Got value '5' with wrong type, expecting object"
  }

  test("a discriminator") {
    import Discriminated._
    val circle: Sum = Circle(1, "x")
    Encoder[Sum].apply(circle).noSpaces shouldBe """{"r":1,"l":"x","type":"Circle"}"""
    Encoder[Sum].apply(Empty: Sum).noSpaces shouldBe """{"type":"Empty"}"""
    Decoder[Sum].decodeJson(json("""{"type":"Circle","r":1,"l":"x"}""")) shouldBe Right(circle)
    Decoder[Sum].decodeJson(json("""{"type":"Empty"}""")) shouldBe Right(Empty)
    errors[Sum]("""{"r":1}""") shouldBe List("DecodingFailure at .type: Missing required field")
    errors[Sum]("""{"type":"Nope"}""") shouldBe List(
      "DecodingFailure at : Unknown case 'Nope', expected one of: Circle, Square, Empty"
    )
    errors[Sum]("""{"type":"Circle","r":"q"}""") shouldBe List(
      "DecodingFailure at .r: Int",
      "DecodingFailure at .l: Missing required field"
    )
  }

  test("defaults are used for missing fields only, when enabled") {
    val withoutDefaults = { import Default._; implicitly[Decoder[Defaults]] }
    withoutDefaults.decodeJson(json("""{"a":1}""")).isLeft shouldBe true
    withoutDefaults.decodeJson(json("""{"a":1,"b":2}""")) shouldBe Right(Defaults(1, 2, None, "t"))
    val withDefaults = { import WithDefaults._; implicitly[Decoder[Defaults]] }
    withDefaults.decodeJson(json("""{"a":1}""")) shouldBe Right(Defaults(1, 5, Some(6), "t"))
    withDefaults.decodeJson(json("""{"a":1,"b":2,"o":null}""")) shouldBe Right(Defaults(1, 2, None, "t"))
    withDefaults.decodeJson(json("""{"a":1,"b":null}""")).isLeft shouldBe true
    withDefaults.decodeJson(json("5")).isLeft shouldBe true
  }

  test("a transient field is neither written nor read") {
    import Default._
    Encoder[Defaults].apply(Defaults(1, 2, None, "written")).noSpaces shouldBe """{"a":1,"b":2,"o":null}"""
    Decoder[Defaults].decodeJson(json("""{"a":1,"b":2,"t":"read"}""")) shouldBe Right(Defaults(1, 2, None, "t"))
  }

  test("None is written as null, or omitted") {
    val writingNulls = { import Default._; implicitly[Encoder[Optional]] }
    writingNulls(Optional(1, None, Some(2))).noSpaces shouldBe """{"a":1,"o":null,"s":2}"""
    val omitting = { import Omitting._; implicitly[Encoder[Optional]] }
    omitting(Optional(1, None, Some(2))).noSpaces shouldBe """{"a":1,"s":2}"""
    omitting(Optional(1, Some(3), Some(2))).noSpaces shouldBe """{"a":1,"o":3,"s":2}"""
    val reading = { import Omitting._; implicitly[Decoder[Optional]] }
    reading.decodeJson(json("""{"a":1,"s":2}""")) shouldBe Right(Optional(1, None, Some(2)))
    reading.decodeJson(json("""{"a":1,"o":null,"s":2}""")) shouldBe Right(Optional(1, None, Some(2)))
  }

  test("a user's codec of an Option type wins over None handling") {
    import Omitting._
    implicit val encoder: Encoder[Option[Boolean]] = Encoder.encodeString.contramap(_.fold("unknown")(_.toString))
    implicitly[Encoder[CustomOption]].apply(CustomOption(None)).noSpaces shouldBe """{"o":"unknown"}"""
  }
}
