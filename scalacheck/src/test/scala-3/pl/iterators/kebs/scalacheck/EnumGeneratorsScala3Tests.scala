package pl.iterators.kebs.scalacheck

import org.scalacheck.rng.Seed
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.enums.{KebsEnum, KebsValueEnum}
import pl.iterators.kebs.scalacheck.model.*

class EnumGeneratorsScala3Tests extends AnyFunSuite with Matchers {
  object Protocol extends KebsArbitrarySupport with KebsEnum with KebsValueEnum
  import Protocol.*

  private def generated[T](using arbitrary: Arbitrary[T]): Set[T] =
    (1 to 200).map(seed => arbitrary.arbitrary.pureApply(Gen.Parameters.default, Seed(seed.toLong))).toSet

  test("enums and value enums") {
    generated[Greeting] shouldBe Greeting.values.toSet
    generated[LongGreeting] shouldBe LongGreeting.values.toSet
    generated[BasicSample].map(_.greeting) shouldBe Greeting.values.toSet
  }
}
