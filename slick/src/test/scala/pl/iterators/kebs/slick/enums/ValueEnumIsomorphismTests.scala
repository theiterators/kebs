//package pl.iterators.kebs.slick.enums
//
//import enumeratum.values.{IntEnum, IntEnumEntry}
//import org.scalatest.funsuite.AnyFunSuite
//import org.scalatest.matchers.should.Matchers
//import pl.iterators.kebs.core.enums.ValueEnumLikeEntry
//import slick.lifted.Isomorphism
//import pl.iterators.kebs.enumeratum.KebsEnumeratum
//
//class ValueEnumIsomorphismTests extends AnyFunSuite with Matchers with KebsEnumeratum {
//
//  sealed abstract class IntGreeting(val value: Int) extends IntEnumEntry with ValueEnumLikeEntry[Int]
//
//  object IntGreeting extends IntEnum[IntGreeting] {
//    case object Hello   extends IntGreeting(0)
//    case object GoodBye extends IntGreeting(1)
//    case object Hi      extends IntGreeting(2)
//    case object Bye     extends IntGreeting(3)
//
//    val values = findValues
//  }
//
//  import IntGreeting._
//
//  test("Implicit isomorphism from ValueEnumEntry") {
//  import pl.iterators.kebs.slick.enums._
//
//    val iso = implicitly[Isomorphism[IntGreeting, Int]]
//    iso.map(Bye) shouldBe 3
//    iso.comap(3) shouldBe Bye
//  }
//
//}
