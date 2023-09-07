import MyEnum.MyEnum
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.enums.EnumLike
import pl.iterators.kebs.enums.KebsEnum
import scala.collection.immutable
import scala.reflect.runtime.universe.typeOf


object MyEnum extends Enumeration {
  type MyEnum = Value
  val Value1, Value2, Value3 = Value
}

class EnumerationTest extends AnyFunSuite with Matchers with KebsEnum {

  def toEnumLike(enumeration: MyEnum)(implicit convertEnumeration: MyEnum.Value => EnumLike[MyEnum.Value]) =
    convertEnumeration(enumeration)

  val enumLike: EnumLike[MyEnum.MyEnum] = implicitly[EnumLike[MyEnum.MyEnum]]
  test("EnumLike[MyEnum.Value].values should return all values of MyEnum") {
    enumLike.values should contain theSameElementsAs Seq(MyEnum.Value1, MyEnum.Value2, MyEnum.Value3)

    enumLike.valueOf("Value1") shouldEqual MyEnum.Value1
  }
}