package pl.iterators.kebs.enums

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.enums.EnumLike
import pl.iterators.kebs.testkit.model.Priority

object EnumerationValuesTestDomain {
  object Ids extends Enumeration {
    val High: Value = Value(3)
    val Low: Value  = Value(1)
    val Mid: Value  = Value(2)
  }
  abstract class WithUnknown extends Enumeration {
    val Unknown: Value = Value
  }
  object Status extends WithUnknown {
    val Active: Value = Value
  }
  object Shade extends Enumeration {
    val Red, Green     = Value
    val Default: Value = Red
  }
  trait Cake {
    object Colors extends Enumeration {
      val Red, Green = Value
    }
  }
  object CakeAll extends Cake
  class Holder {
    object Inner extends Enumeration {
      val A, B = Value
    }
  }
}

// kebs 2 listed Obj.values: id order, inherited values, no duplicates for alias vals
class EnumerationValuesTests extends AnyFunSuite with Matchers with KebsEnum {
  import EnumerationValuesTestDomain._

  test("values follow the ids, as Obj.values") {
    implicitly[EnumLike[Ids.Value]].values shouldBe List(Ids.Low, Ids.Mid, Ids.High)
  }

  test("values inherited from a parent Enumeration class") {
    implicitly[EnumLike[Status.Value]].withNameOption("Unknown") shouldBe Some(Status.Unknown)
  }

  test("an alias val is not listed twice") {
    implicitly[EnumLike[Shade.Value]].values shouldBe List(Shade.Red, Shade.Green)
  }

  test("Enumeration declared in a trait mixed into an object") {
    implicitly[EnumLike[CakeAll.Colors.Value]].values shouldBe List(CakeAll.Colors.Red, CakeAll.Colors.Green)
  }

  test("Enumeration declared in a class") {
    val holder = new Holder
    implicitly[EnumLike[holder.Inner.Value]].values shouldBe List(holder.Inner.A, holder.Inner.B)
  }

  test("Enumeration compiled in another module") {
    implicitly[EnumLike[Priority.Value]].values shouldBe Priority.values.toList
  }
}
