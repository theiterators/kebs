package pl.iterators.kebs.enumeratum

import enumeratum.{Enum, EnumEntry}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.enums.EnumLike

trait EnumeratumCake {
  sealed trait Fruit extends EnumEntry
  object Fruit       extends Enum[Fruit] {
    case object Apple extends Fruit
    case object Pear  extends Fruit
    val values = findValues
  }
}
object EnumeratumCakeAll extends EnumeratumCake

// enumeratum's findValues is empty here on Scala 3; kebs 2 listed the case objects through the Mirror
class EnumeratumCakeTests extends AnyFunSuite with Matchers with KebsEnumeratum {
  import EnumeratumCakeAll._

  test("enumeratum enum declared in a trait mixed into an object") {
    implicitly[EnumLike[Fruit]].values shouldBe List(Fruit.Apple, Fruit.Pear)
  }
}
