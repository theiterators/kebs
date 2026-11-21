package pl.iterators.kebs.core

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass

object ValueClassLikeScala2SelectionDomain {
  final case class PrivateField(private val value: Int)
}

/** On Scala 2 a private case field has no readable accessor: kebs 2 gave no instance, silently; kebs 3 keeps that. */
class ValueClassLikeScala2SelectionTests extends AnyFunSuite with Matchers with CaseClass1ToValueClass {
  import ValueClassLikeScala2SelectionDomain._

  test("case class with a private field falls back silently") {
    implicitly[Pick[PrivateField]].chosen shouldBe "fallback"
  }
}
