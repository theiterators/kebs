import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.enumeratum.KebsEnumeratum
import pl.iterators.kebs.enums.EnumLike
import enumeratum._

sealed trait MyEnum extends EnumEntry
object MyEnum extends Enum[MyEnum] {
  case object Value1 extends MyEnum
  case object Value2 extends MyEnum
  case object Value3 extends MyEnum

  val values: IndexedSeq[MyEnum] = findValues
}

class MyEnumTest extends AnyFunSuite with Matchers with KebsEnumeratum {

  val enumLike: EnumLike[MyEnum] = implicitly[EnumLike[MyEnum]]

  test("EnumLike[MyEnum.type].values should return all values of MyEnum") {
    enumLike.values should contain theSameElementsAs Seq(MyEnum.Value1, MyEnum.Value2, MyEnum.Value3)
  }
}