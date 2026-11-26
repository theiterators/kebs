package pl.iterators.kebs.scalacheck

import enumeratum.{Enum, EnumEntry}
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.rng.Seed
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.enumeratum.{KebsEnumeratum, KebsValueEnumeratum}
import pl.iterators.kebs.enums.KebsEnum
import pl.iterators.kebs.testkit.model.{Color, Level, Paint, Priority}

object EnumGeneratorsTestDomain {
  sealed abstract class Stage extends EnumEntry
  object Stage                extends Enum[Stage] {
    case object Current extends Stage
    case object Retired extends Stage
    val values: IndexedSeq[Stage] = Vector(Current)
  }
  final case class Holder(stage: Stage, stages: List[Stage], color: Option[Color], level: Level)
}

class EnumGeneratorsTests extends AnyFunSuite with Matchers {
  import EnumGeneratorsTestDomain._

  object Enumeratum   extends KebsArbitrarySupport with KebsEnumeratum with KebsValueEnumeratum
  object Enumerations extends KebsArbitrarySupport with KebsEnum
  object Derived      extends KebsArbitrarySupport

  private def generated[T](implicit arbitrary: Arbitrary[T]): Set[T] =
    (1 to 200).map(seed => arbitrary.arbitrary.pureApply(Gen.Parameters.default, Seed(seed.toLong))).toSet

  test("Enumerations") {
    import Enumerations._
    generated[Priority.Value] shouldBe Priority.values.toSet
  }

  test("enumeratum enums and value enums") {
    import Enumeratum._
    generated[Color] shouldBe Color.values.toSet
    generated[Level] shouldBe Level.values.toSet
    generated[Paint].size shouldBe Color.values.size * Level.values.size
  }

  test("only the values of an enum") {
    import Enumeratum._
    generated[Stage] shouldBe Set(Stage.Current)
    val holders = generated[Holder]
    holders.map(_.stage) shouldBe Set(Stage.Current)
    holders.flatMap(_.stages) shouldBe Set(Stage.Current)
    holders.flatMap(_.color) shouldBe Color.values.toSet
  }

  test("derived where there is no EnumLike, as before") {
    import Derived._
    generated[Color] shouldBe Color.values.toSet
    generated[Stage] shouldBe Set(Stage.Current, Stage.Retired)
  }
}
