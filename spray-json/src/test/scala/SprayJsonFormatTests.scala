import java.util.UUID

import org.scalatest.{FunSuite, Matchers}
import pl.iterators.kebs.json.{KebsSpray, noflat}
import spray.json._

class SprayJsonFormatTests extends FunSuite with Matchers {
  object KebsProtocol extends DefaultJsonProtocol with KebsSpray
  import KebsProtocol._

  case class C(i: Int)
  case class D(i: Int, s: String)
  case class E(noFormat: UUID)
  case object F

  case class DTO1(c: C)
  case class DTO2(c: Option[C])
  case class Compound(c: C, d: D)

  case class Parametrized1[T](field: T)
  case class Parametrized2[T0, T1](field1: T0, field2: T1)

  test("Flat format") {
    val jf = implicitly[JsonFormat[C]]
    jf.write(C(10)) shouldBe JsNumber(10)
    jf.read(JsNumber(10)) shouldBe C(10)
  }

  test("Flat format - no implicit JsonFormat") {
    "implicitly[JsonFormat[E]]" shouldNot compile
  }

  test("Flat format - parametrized") {
    val jf = implicitly[JsonFormat[Parametrized1[Double]]]
    jf.write(Parametrized1(15.0)) shouldBe JsNumber(15.0)
    jf.read(JsNumber(15.0)) shouldBe Parametrized1(15.0)
  }

  test("Root format 0") {
    val jf = implicitly[RootJsonFormat[F.type]]
    jf.write(F) shouldBe JsObject()
    jf.read(JsObject()) shouldBe F
  }

  test("Root format 1") {
    val jf = implicitly[RootJsonFormat[C]]
    jf.write(C(10)) shouldBe JsObject("i" -> JsNumber(10))
    jf.read(JsObject("i" -> JsNumber(0))) shouldBe C(0)
  }

  test("Root format 2") {
    val jf = implicitly[RootJsonFormat[D]]
    jf.write(D(10, "abcd")) shouldBe JsObject("i" -> JsNumber(10), "s" -> JsString("abcd"))
    jf.read(JsObject("i" -> JsNumber(5), "s" -> JsString("abcdef"))) shouldBe D(5, "abcdef")
  }

  test("Root format - no implicit JsonFormat") {
    "implicitly[RootJsonFormat[E]]" shouldNot compile
  }

  test("Root format - parametrized") {
    val jf = implicitly[RootJsonFormat[Parametrized2[Int, String]]]
    jf.write(Parametrized2(10, "abcd")) shouldBe JsObject("field1" -> JsNumber(10), "field2" -> JsString("abcd"))
    jf.read(JsObject("field1" -> JsNumber(5), "field2" -> JsString("abcdef"))) shouldBe Parametrized2(5, "abcdef")
  }

  test("Json format 2") {
    val jf = implicitly[JsonFormat[D]]
    jf.write(D(10, "abcd")) shouldBe JsObject("i" -> JsNumber(10), "s" -> JsString("abcd"))
    jf.read(JsObject("i" -> JsNumber(5), "s" -> JsString("abcdef"))) shouldBe D(5, "abcdef")
  }

  test("Root format - DTO style") {
    val jf = implicitly[RootJsonFormat[DTO1]]
    jf.write(DTO1(C(10))) shouldBe JsObject("c" -> JsNumber(10))
    jf.read(JsObject("c" -> JsNumber(10))) shouldBe DTO1(C(10))
  }

  test("Root format - DTO style with Option") {
    val jf = implicitly[RootJsonFormat[DTO2]]
    jf.write(DTO2(Some(C(10)))) shouldBe JsObject("c" -> JsNumber(10))
    jf.read(JsObject()) shouldBe DTO2(None)
  }

  test("Root format - compound") {
    val jf = implicitly[JsonFormat[Compound]]
    jf.write(Compound(C(5), D(10, "abcd"))) shouldBe JsObject("c" -> JsNumber(5),
                                                              "d" -> JsObject("i" -> JsNumber(10), "s" -> JsString("abcd")))
    jf.read(JsObject("c" -> JsNumber(10), "d" -> JsObject("i" -> JsNumber(100), "s" -> JsString("abb")))) shouldBe Compound(C(10),
                                                                                                                            D(100, "abb"))
  }

  case class Inner(a: Int)
  case class Wrapper(a: Inner)
  case class Holder(a: Wrapper)

  test("bug: value <none>") {
    val anything = 0
    anything match {
      case 0 => implicitly[RootJsonFormat[Holder]]
      case _ => ()
    }
  }

  case class Thing(thingId: String, parent: Option[Thing])
  implicit val thingFormat: RootJsonFormat[Thing] = jsonFormatRec[Thing]

  test("bug: mutually recursive") {
    val t  = Thing("child", Some(Thing("parent", None)))
    val jf = implicitly[JsonFormat[Thing]]
    jf.write(t) shouldBe JsObject("thingId" -> JsString("child"), "parent" -> JsObject("thingId" -> JsString("parent")))
    jf.read(
      JsObject(
        "thingId" -> JsString("child"),
        "parent"  -> JsObject("thingId" -> JsString("parent"), "parent" -> JsObject("thingId" -> JsString("grandparent"))))) shouldBe Thing(
      "child",
      Some(Thing("parent", Some(Thing("grandparent", None)))))
  }

  case class Book(name: String, chapters: List[Chapter])
  case class Chapter(name: String)

  implicit val chapterRootFormat: RootJsonFormat[Chapter] = jsonFormatN[Chapter]

  test("work with nested single field objects") {
    val json =
      """
        | {
        |   "name": "Functional Programming in Scala",
        |   "chapters": [{"name":"first"}, {"name":"second"}]
        | }
      """.stripMargin

    json.parseJson.convertTo[Book] shouldBe Book(
      name = "Functional Programming in Scala",
      chapters = List(Chapter("first"), Chapter("second"))
    )
  }

  case class BookNF(name: String, chapters: List[ChapterNF])
  @noflat case class ChapterNF(name: String)

  test("work with nested single field objects - noflat annotation") {
    val json = JsObject(
      "name" -> JsString("Functional Programming in Scala"),
      "chapters" -> JsArray(
        JsObject("name" -> JsString("first")),
        JsObject("name" -> JsString("second"))
      )
    )
    val instance = BookNF(
      name = "Functional Programming in Scala",
      chapters = List(ChapterNF("first"), ChapterNF("second"))
    )

    val jf = implicitly[RootJsonFormat[BookNF]]
    jf.read(json) shouldBe instance
    jf.write(instance) shouldBe json
  }

  test("Root format - case class with > 22 fields (issue #7)") {
    import model._

    val jf = implicitly[JsonFormat[ClassWith23Fields]]
    val obj = ClassWith23Fields(
      F1("f1 value"),
      2,
      3L,
      None,
      Some("f5 value"),
      "six",
      List("f7 value 1", "f7 value 2"),
      "f8 value",
      "f9 value",
      "f10 value",
      "f11 value",
      "f12 value",
      "f13 value",
      "f14 value",
      "f15 value",
      "f16 value",
      "f17 value",
      "f18 value",
      "f19 value",
      "f20 value",
      "f21 value",
      "f22 value",
      true
    )
    val json = JsObject(
      Map(
        "f1"             -> JsString("f1 value"),
        "f2"             -> JsNumber(2),
        "f3"             -> JsNumber(3),
        "f4"             -> JsNull,
        "f5"             -> JsString("f5 value"),
        "fieldNumberSix" -> JsString("six"),
        "f7"             -> JsArray(JsString("f7 value 1"), JsString("f7 value 2")),
        "f8"             -> JsString("f8 value"),
        "f9"             -> JsString("f9 value"),
        "f10"            -> JsString("f10 value"),
        "f11"            -> JsString("f11 value"),
        "f12"            -> JsString("f12 value"),
        "f13"            -> JsString("f13 value"),
        "f14"            -> JsString("f14 value"),
        "f15"            -> JsString("f15 value"),
        "f16"            -> JsString("f16 value"),
        "f17"            -> JsString("f17 value"),
        "f18"            -> JsString("f18 value"),
        "f19"            -> JsString("f19 value"),
        "f20"            -> JsString("f20 value"),
        "f21"            -> JsString("f21 value"),
        "f22"            -> JsString("f22 value"),
        "f23"            -> JsBoolean(true)
      ))

    jf.write(obj) shouldBe json
    jf.read(json) shouldBe obj
  }

  case class Request(region: String, currency: String, date: String, code: String, items: List[RequestItem])
  case class RequestItem(itemCode: String, language: String, answers: Option[List[Answer]], requirements: String, travellers: String)
  case class Answer(answer: String)

  test("issue #11") {
    "implicitly[JsonFormat[Request]]" should compile
  }

  case class Category(
      name: String,
      parent: Option[Category]
  )

  test("issue #21") {
    """implicit val categoryFormat = jsonFormatRec[Category]""" should compile
  }

}
