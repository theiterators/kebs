package pl.iterators.kebs.playjson.formats

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.playjson.KebsPlayJson
import pl.iterators.kebs.testkit.validation._
import play.api.libs.json._

class PlayValidationTests extends AnyFunSuite with Matchers {
  object Protocol extends KebsPlayJson with CaseClass1ToValueClass
  // a hand-written instance loses to CaseClass1ToValueClass in lexical scope
  object Plain extends KebsPlayJson

  private def failure[T: Reads](json: String): String = Json.parse(json).validate[T] match {
    case JsError(errors) => errors.flatMap(_._2).flatMap(_.messages).mkString(", ")
    case ok              => fail(s"expected an error, got $ok")
  }

  test("a value class's own from decides") {
    import Plain._
    Json.parse("\"abc\"").as[Code] shouldBe Code("abc")
    failure[Code]("\"abcd\"") should include("not three letters")
  }

  test("a rejecting apply is a JsError") {
    import Protocol._
    failure[Positive]("0") should include("not positive")
  }

  test("a case class whose constructor rejects the fields is a JsError") {
    import Protocol._
    failure[Span]("""{"start":2,"end":1}""") should include("start after end")
    failure[HasSpan]("""{"span":{"start":2,"end":1},"codes":[]}""") should include("start after end")
    Json.parse("""{"span":{"start":1,"end":2},"codes":["abc"]}""").as[HasSpan] shouldBe HasSpan(Span(1, 2), List(Code("abc")))
  }
}
