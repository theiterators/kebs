package pl.iterators.kebs.enumeratum

import enumeratum.{Enum, EnumEntry}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.enums.EnumLike

object EnumeratumNestedTestDomain {
  sealed trait Animal extends EnumEntry
  sealed trait Mammal extends Animal
  object Animal       extends Enum[Animal] {
    case object Dog  extends Mammal
    case object Cat  extends Mammal
    case object Fish extends Animal
    val values = findValues
  }
}

class EnumeratumNestedTests extends AnyFunSuite with Matchers with KebsEnumeratum {
  import EnumeratumNestedTestDomain._

  test("nested sealed hierarchy lists the companion's values") {
    val animals = implicitly[EnumLike[Animal]]
    animals.values shouldBe Animal.values
    animals.names shouldBe List("Dog", "Cat", "Fish")
  }
}
