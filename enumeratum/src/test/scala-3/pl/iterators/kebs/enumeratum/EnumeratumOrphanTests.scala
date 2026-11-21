package pl.iterators.kebs.enumeratum

import enumeratum.EnumEntry
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.enums.EnumLike

object EnumeratumOrphanTestDomain {
  sealed trait Orphan extends EnumEntry
  case object O1      extends Orphan
  case object O2      extends Orphan
}

/** Scala 3: kebs 2 listed the case objects through the Mirror even without an Enum companion; kebs 3 keeps that. */
class EnumeratumOrphanTests extends AnyFunSuite with Matchers with KebsEnumeratum {
  import EnumeratumOrphanTestDomain._

  test("EnumEntry without an Enum companion still gets an EnumLike of its case objects") {
    implicitly[PickEnum[Orphan]].chosen shouldBe "enum"
    implicitly[EnumLike[Orphan]].values shouldBe List(O1, O2)
  }
}
