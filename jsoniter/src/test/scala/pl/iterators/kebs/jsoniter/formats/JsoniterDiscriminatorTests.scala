package pl.iterators.kebs.jsoniter.formats

import com.github.plokhotnyuk.jsoniter_scala.core._
import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.core.naming.{Naming => NamingStyle}
import pl.iterators.kebs.jsoniter.KebsJsoniter
import pl.iterators.kebs.testkit.model._

object JsoniterDiscriminatorTestDomain {
  sealed trait Expr
  final case class Num(value: Int, note: Option[String]) extends Expr
  final case class Add(left: Expr, right: Expr)          extends Expr
  final case class All(exprs: List[Expr], n: Int)        extends Expr
  case object Zero                                       extends Expr

  sealed trait Typed
  final case class Named(`type`: String, n: Int) extends Typed
  final case class Flat(n: Int)                  extends Typed

  sealed trait Foreign
  final case class Object(a: Int, b: Int) extends Foreign
  object Object { implicit val codec: JsonValueCodec[Object] = JsoniterNestedInstancesTestDomain.named("AB", ab)(identity, identity) }
  final case class Nothing_(n: Int, m: Int) extends Foreign
  object Nothing_ { implicit val codec: JsonValueCodec[Nothing_] = written("{}", Nothing_(0, 0)) }
  final case class Text(n: Int, m: Int) extends Foreign
  object Text { implicit val codec: JsonValueCodec[Text] = written("\"text\"", Text(0, 0)) }
  final case class Clash(n: Int, m: Int) extends Foreign
  object Clash { implicit val codec: JsonValueCodec[Clash] = written("""{"a":{"type":1},"type":"x"}""", Clash(0, 0)) }

  private def ab: JsonValueCodec[Object] =
    JsoniterNestedInstancesTestDomain.asString(s => Object(s.take(1).toInt, s.drop(1).toInt), o => s"${o.a}${o.b}")

  private def written[A](json: String, value: A): JsonValueCodec[A] = new JsonValueCodec[A] {
    def decodeValue(in: JsonReader, default: A): A = { in.skip(); value }
    def encodeValue(x: A, out: JsonWriter): Unit   = out.writeRawVal(json.getBytes("UTF-8"))
    def nullValue: A                               = null.asInstanceOf[A]
  }
}

class JsoniterDiscriminatorTests extends JsoniterTestBase {
  import JsoniterDiscriminatorTestDomain._

  object Protocol extends KebsJsoniter {
    override def kebsJsonConfig: JsonConfig = super.kebsJsonConfig.withDiscriminator("type")
  }
  object FlatProtocol extends KebsJsoniter with CaseClass1ToValueClass {
    override def kebsJsonConfig: JsonConfig = super.kebsJsonConfig.withDiscriminator("type")
  }
  object Kinds extends KebsJsoniter {
    override def kebsJsonConfig: JsonConfig =
      super.kebsJsonConfig.withDiscriminator("kind \"ż\"").withConstructorNaming(NamingStyle.snakeCase)
  }

  test("is written first") {
    import Protocol._
    roundTrip[Shape](Circle(1.0, "c"), """{"type":"Circle","r":1.0,"label":"c"}""")
    roundTrip[Shape](Empty, """{"type":"Empty"}""")
    roundTrip[List[Shape]](List(Empty, Circle(1.0, "c")), """[{"type":"Empty"},{"type":"Circle","r":1.0,"label":"c"}]""")
  }

  test("any name, and constructor naming") {
    import Kinds._
    roundTrip[Shape](Circle(1.0, "c"), """{"kind \"ż\"":"circle","r":1.0,"label":"c"}""")
  }

  test("is read from any position") {
    import Protocol._
    readFromString[Shape]("""{"r":1.0,"type":"Circle","label":"c"}""") shouldBe Circle(1.0, "c")
    readFromString[Shape](""" { "r" : 1.0 , "label" : "c" , "x" : {"type":"Empty"} , "type" : "Circle" } """) shouldBe Circle(1.0, "c")
    readFromString[Shape]("""{"x":[],"type":"Empty","y":{}}""") shouldBe Empty
  }

  test("nested") {
    import Protocol._
    val expr: Expr = Add(All(List(Zero, Num(1, None), Add(Zero, Zero)), 2), Num(3, Some("n")))
    roundTrip[Expr](
      expr,
      """{"type":"Add","left":{"type":"All","exprs":[{"type":"Zero"},{"type":"Num","value":1,"note":null},""" +
        """{"type":"Add","left":{"type":"Zero"},"right":{"type":"Zero"}}],"n":2},"right":{"type":"Num","value":3,"note":"n"}}"""
    )
    readFromString[Expr](
      """{"right":{"note":"n","value":3,"type":"Num"},"left":{"exprs":[{"type":"Zero"},{"value":1,"type":"Num"},""" +
        """{"right":{"type":"Zero"},"left":{"type":"Zero"},"type":"Add"}],"n":2,"type":"All"},"type":"Add"}"""
    ) shouldBe expr
  }

  test("from a stream, in pieces") {
    import Protocol._
    val json = """{"left":{"value":1,"type":"Num","note":"""" + "x" * 100000 + """"},"right":{"type":"Zero"},"type":"Add"}"""
    val in   = new java.io.ByteArrayInputStream(json.getBytes("UTF-8")) {
      override def read(b: Array[Byte], off: Int, len: Int): Int = super.read(b, off, math.min(len, 7))
    }
    readFromStream[Expr](in) shouldBe Add(Num(1, Some("x" * 100000)), Zero)
  }

  test("what is not a case") {
    import Protocol._
    rejected[Shape]("{}", "\"type\"")
    rejected[Shape]("""{"r":1.0,"label":"c"}""", "\"type\"")
    rejected[Shape]("""{"type":"Square"}""", "unknown case 'Square', expected one of: Circle, Empty")
    rejected[Shape]("""{"type":1}""", "")
    rejected[Shape]("""{"type":null}""", "")
    rejected[Shape]("""{"type":{"type":"Empty"}}""", "")
    rejected[Shape]("\"Empty\"", "")
    rejected[Shape]("null", "")
    rejected[Shape]("""{"type":"Circle","r":1.0}""", "\"label\"")
    (0 until 15).foreach(length => rejected[Shape]("""{"type":"Empty"}""".take(length), ""))
  }

  test("a case that cannot carry it") {
    (the[JsonWriterException] thrownBy {
      import Protocol._
      writeToString[Typed](Named("n", 1))
    }).getMessage should include("Named has a field 'type' of its own, so it cannot carry the 'type' discriminator")

    (the[JsonWriterException] thrownBy {
      import FlatProtocol._
      writeToString[Typed](Flat(1))
    }).getMessage should include("Flat is not written as a JSON object, so it cannot carry the 'type' discriminator")
  }

  test("a case with a user's codec") {
    import Protocol._
    writeToString[Foreign](Object(1, 2)) shouldBe """{"type":"Object","AB":"12"}"""
    writeToString[Foreign](Nothing_(1, 2)) shouldBe """{"type":"Nothing_"}"""
    readFromString[Foreign]("""{"type":"Nothing_"}""") shouldBe Nothing_(0, 0)
    (the[JsonWriterException] thrownBy writeToString[Foreign](Text(1, 2))).getMessage should include(
      "Text is not written as a JSON object"
    )
    (the[JsonWriterException] thrownBy writeToString[Foreign](Clash(1, 2))).getMessage should include(
      "Clash has a field 'type' of its own"
    )
  }
}
