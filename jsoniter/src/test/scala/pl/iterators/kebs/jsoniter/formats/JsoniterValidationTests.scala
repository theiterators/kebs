package pl.iterators.kebs.jsoniter.formats

import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.jsoniter.KebsJsoniter
import pl.iterators.kebs.testkit.validation._

class JsoniterValidationTests extends JsoniterTestBase {
  object Protocol extends KebsJsoniter with CaseClass1ToValueClass
  // a hand-written instance loses to CaseClass1ToValueClass in lexical scope
  object Plain extends KebsJsoniter

  test("a value class's own from decides") {
    import Plain._
    roundTrip(Code("abc"), "\"abc\"")(deriveCodec[Code])
    rejected[Code]("\"abcd\"", "not three letters")(deriveCodec[Code])
    rejected[Map[Code, Int]]("""{"abcd":1}""", "not three letters")(deriveCodec[Map[Code, Int]])
  }

  test("a rejecting apply is a decoding error") {
    import Protocol._
    rejected[Positive]("0", "not positive")(deriveCodec[Positive])
  }

  test("a case class whose constructor rejects the fields is a decoding error") {
    import Protocol._
    rejected[Span]("""{"start":2,"end":1}""", "start after end")(deriveCodec[Span])
    rejected[HasSpan]("""{"span":{"start":2,"end":1},"codes":[]}""", "start after end")(deriveCodec[HasSpan])
    roundTrip(HasSpan(Span(1, 2), List(Code("abc"))), """{"span":{"start":1,"end":2},"codes":["abc"]}""")(deriveCodec[HasSpan])
  }
}
