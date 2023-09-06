import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.enums.{EnumLike, KebsEnum}

object MyEnum extends Enumeration {
  type MyEnum = Value
  val Value1, Value2, Value3 = Value
}

class MyEnumTest extends AnyFunSuite with Matchers with KebsEnum {

  val enumLike: EnumLike[MyEnum.type] = implicitly[EnumLike[MyEnum.type]]

  test("EnumLike[MyEnum.type].values should return all values of MyEnum") {
    enumLike.values should contain theSameElementsAs Seq(MyEnum.Value1, MyEnum.Value2, MyEnum.Value3)
  }
}
