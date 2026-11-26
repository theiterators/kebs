package pl.iterators.kebs.core

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.macros.{ValueClass, ValueClassLike}

object ValueClassDerivesTestDomain {
  final case class Email(value: String) derives ValueClass
  final case class Id(value: Long) extends AnyVal derives ValueClass
}

class ValueClassDerivesTests extends AnyFunSuite with Matchers {
  import ValueClassDerivesTestDomain.*

  test("derives ValueClass") {
    summon[ValueClassLike[Email, String]].apply("a") shouldBe Email("a")
    summon[ValueClassLike[Id, Long]].unapply(Id(2L)) shouldBe 2L
  }

  test("derives ValueClass needs exactly one field") {
    "final case class Two(a: Int, b: Int) derives ValueClass" shouldNot compile
  }
}
