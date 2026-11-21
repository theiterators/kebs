package pl.iterators.kebs.enumeratum

import enumeratum.EnumEntry
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

object EnumeratumOrphanTestDomain {
  sealed trait Orphan extends EnumEntry
  case object O1      extends Orphan
  case object O2      extends Orphan
}

/** Scala 2: an EnumEntry without an Enum companion gets no EnumLike, silently (unchanged from kebs 2). */
class EnumeratumOrphanTests extends AnyFunSuite with Matchers with KebsEnumeratum {
  import EnumeratumOrphanTestDomain._

  test("EnumEntry without an Enum companion falls back silently") {
    implicitly[PickEnum[Orphan]].chosen shouldBe "fallback"
  }
}
