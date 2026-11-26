package pl.iterators.kebs.enums

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.enums.{EnumLike, ValueEnumLike, ValueEnumLikeEntry}

trait EnumCake {
  enum Level { case A, B }
  enum Rgb(val value: Int) extends ValueEnumLikeEntry[Int] {
    case R extends Rgb(1)
    case G extends Rgb(2)
  }
  sealed abstract class Size(val value: Int) extends ValueEnumLikeEntry[Int]
  object Size {
    case object S extends Size(1)
    case object M extends Size(2)
  }
}
object EnumCakeAll extends EnumCake

// kebs 2 derived these through the Mirror
class CakeEnumTests extends AnyFunSuite with Matchers with KebsEnum with KebsValueEnum {
  import EnumCakeAll._

  test("Scala 3 enum declared in a trait mixed into an object") {
    implicitly[EnumLike[Level]].values shouldBe List(Level.A, Level.B)
  }

  test("Scala 3 value enum declared in a trait mixed into an object") {
    implicitly[ValueEnumLike[Int, Rgb]].values shouldBe List(Rgb.R, Rgb.G)
  }

  test("sealed value enum declared in a trait mixed into an object") {
    implicitly[ValueEnumLike[Int, Size]].values shouldBe List(Size.S, Size.M)
  }
}
