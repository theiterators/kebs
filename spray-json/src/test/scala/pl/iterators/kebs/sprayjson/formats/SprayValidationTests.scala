package pl.iterators.kebs.sprayjson.formats

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.sprayjson.KebsSprayJson
import pl.iterators.kebs.testkit.validation._
import spray.json._

class SprayValidationTests extends AnyFunSuite with Matchers {
  object Protocol extends DefaultJsonProtocol with KebsSprayJson with CaseClass1ToValueClass

  private def failure[T: JsonReader](json: String): String =
    intercept[DeserializationException](json.parseJson.convertTo[T]).getMessage

  test("a value class's own from decides, also next to CaseClass1ToValueClass") {
    import Protocol._
    "\"abc\"".parseJson.convertTo[Code] shouldBe Code("abc")
    failure[Code]("\"abcd\"") should include("not three letters")
  }

  test("a rejecting apply is a DeserializationException") {
    import Protocol._
    failure[Positive]("0") should include("not positive")
  }

  test("a case class whose constructor rejects the fields is a DeserializationException") {
    import Protocol._
    failure[Span]("""{"start":2,"end":1}""") should include("start after end")
    failure[HasSpan]("""{"span":{"start":2,"end":1},"codes":[]}""") should include("start after end")
    """{"span":{"start":1,"end":2},"codes":["abc"]}""".parseJson.convertTo[HasSpan] shouldBe HasSpan(Span(1, 2), List(Code("abc")))
  }
}
