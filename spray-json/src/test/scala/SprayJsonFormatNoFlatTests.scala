//import pl.iterators.kebs.json.KebsSpray
//import spray.json._
//import org.scalatest.funsuite.AnyFunSuite
//import org.scalatest.matchers.should.Matchers
//
//class SprayJsonFormatNoFlatTests extends AnyFunSuite with Matchers {
//  object KebsProtocol extends DefaultJsonProtocol with KebsSpray.NoFlat
//  import KebsProtocol._
//
//  case class C(i: Int)
//
//  test("No-flat format") {
//    val jf = implicitly[JsonFormat[C]]
//    jf.write(C(10)) shouldBe JsObject("i" -> JsNumber(10))
//    jf.read(JsObject("i" -> JsNumber(10))) shouldBe C(10)
//  }
//
//  case class Book(name: String, chapters: List[Chapter])
//  case class Chapter(name: String)
//
//  test("compound") {
//    val json =
//      """
//        | {
//        |   "name": "Functional Programming in Scala",
//        |   "chapters": [{"name":"first"}, {"name":"second"}]
//        | }
//      """.stripMargin
//
//    json.parseJson.convertTo[Book] shouldBe Book(
//      name = "Functional Programming in Scala",
//      chapters = List(Chapter("first"), Chapter("second"))
//    )
//  }
//}
