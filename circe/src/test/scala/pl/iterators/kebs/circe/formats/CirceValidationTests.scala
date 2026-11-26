package pl.iterators.kebs.circe.formats

import io.circe.Decoder
import io.circe.parser.decode
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.circe.KebsCirce
import pl.iterators.kebs.core.instances.InstanceConverter
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.testkit.validation._

class CirceValidationTests extends AnyFunSuite with Matchers {
  object Protocol extends KebsCirce with CaseClass1ToValueClass

  private def failure[T: Decoder](json: String) = decode[T](json).left.map(_.getMessage)

  test("a value class's own from decides, also next to CaseClass1ToValueClass") {
    import Protocol._
    decode[Code]("\"abc\"") shouldBe Right(Code("abc"))
    failure[Code]("\"abcd\"").left.toOption.get should include("not three letters")
  }

  test("a rejecting apply is a decoding failure") {
    import Protocol._
    failure[Positive]("0").left.toOption.get should include("not positive")
  }

  test("a case class whose constructor rejects the fields is a decoding failure") {
    import Protocol._
    failure[Span]("""{"start":2,"end":1}""").left.toOption.get should include("start after end")
    failure[HasSpan]("""{"span":{"start":2,"end":1},"codes":[]}""").left.toOption.get should include("start after end")
    decode[HasSpan]("""{"span":{"start":1,"end":2},"codes":["abc"]}""") shouldBe Right(HasSpan(Span(1, 2), List(Code("abc"))))
  }

  test("an InstanceConverter's decodeEither decides") {
    import Protocol._
    implicit val converter: InstanceConverter[Span, String] = new InstanceConverter[Span, String] {
      def encode(obj: Span): String                                  = s"${obj.start}-${obj.end}"
      def decode(value: String): Span                                = Span(0, 0)
      override def decodeEither(value: String): Either[String, Span] = Left("no spans")
    }
    failure[Span]("\"1-2\"").left.toOption.get should include("no spans")
  }
}
