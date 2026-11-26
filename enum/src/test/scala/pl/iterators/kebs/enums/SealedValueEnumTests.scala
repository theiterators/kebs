package pl.iterators.kebs.enums

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.enums.{ValueEnumLike, ValueEnumLikeEntry}

object SealedValueEnumTestDomain {
  sealed abstract class Size(val value: Int) extends ValueEnumLikeEntry[Int]
  case object S                              extends Size(1)
  case object M                              extends Size(2)
  case object L                              extends Size(3)
}

class SealedValueEnumTests extends AnyFunSuite with Matchers with KebsValueEnum {
  import SealedValueEnumTestDomain._

  test("ValueEnumLike for a sealed hierarchy of case objects") {
    val sizes = implicitly[ValueEnumLike[Int, Size]]
    sizes.values shouldBe List(S, M, L)
    sizes.withValue(2) shouldBe M
  }
}
