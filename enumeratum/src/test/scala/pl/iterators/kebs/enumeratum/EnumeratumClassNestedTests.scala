package pl.iterators.kebs.enumeratum

import enumeratum.{Enum, EnumEntry}
import enumeratum.values.{IntEnum, IntEnumEntry}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.enums.{EnumLike, ValueEnumLike, ValueEnumLikeEntry}

// enumeratum's Scala 3 findValues finds nothing for enums declared inside a class
class EnumeratumClassNestedTests extends AnyFunSuite with Matchers with KebsEnumeratum with KebsValueEnumeratum {
  sealed trait Greeting extends EnumEntry
  object Greeting       extends Enum[Greeting] {
    val values = findValues
    case object Hello extends Greeting
    case object Bye   extends Greeting
  }

  sealed abstract class Rank(val value: Int) extends IntEnumEntry with ValueEnumLikeEntry[Int]
  object Rank                                extends IntEnum[Rank] {
    val values = findValues
    case object First  extends Rank(1)
    case object Second extends Rank(2)
  }

  test("class-nested enum lists its entries") {
    implicitly[EnumLike[Greeting]].values shouldBe List(Greeting.Hello, Greeting.Bye)
  }

  test("class-nested value enum lists its entries") {
    implicitly[ValueEnumLike[Int, Rank]].values shouldBe List(Rank.First, Rank.Second)
  }
}
