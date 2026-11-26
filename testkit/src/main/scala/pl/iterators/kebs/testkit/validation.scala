package pl.iterators.kebs.testkit

import pl.iterators.kebs.core.macros.ValueClassLike

object validation {
  // apply accepts anything; only the hand-written `from` checks
  final case class Code(value: String)
  object Code {
    implicit val valueClassLike: ValueClassLike[Code, String] =
      new ValueClassLike[Code, String](Code(_), _.value, s => if (s.length == 3) Right(Code(s)) else Left("not three letters"))
  }

  final case class Positive(value: Int) { require(value > 0, "not positive") }

  final case class Span(start: Int, end: Int) { require(start <= end, "start after end") }
  final case class HasSpan(span: Span, codes: List[Code])
}
