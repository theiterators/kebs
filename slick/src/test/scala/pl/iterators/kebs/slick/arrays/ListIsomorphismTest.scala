//package pl.iterators.kebs.slick.arrays
//
//import org.scalatest.funsuite.AnyFunSuite
//import org.scalatest.matchers.should.Matchers
//import pl.iterators.kebs.instances.time.YearMonthString
//import slick.lifted.Isomorphism
//
//class ListIsomorphismTest extends AnyFunSuite with Matchers with YearMonthString {
//  import pl.iterators.kebs.slick._
//
//  case class C(a: String)
//
//  test("No ValueClassLike implicits derived") {
//    import pl.iterators.kebs.core.macros.ValueClassLike
//
//    "implicitly[ValueClassLike[YearMonth, String]]" shouldNot typeCheck
//    "implicitly[ValueClassLike[String, YearMonth]]" shouldNot typeCheck
//  }
//
//  test("Case class isomorphism implies list isomorphism") {
//    val iso = implicitly[Isomorphism[List[C], List[String]]]
//    iso.map(List(C("a"), C("b"))) shouldBe List("a", "b")
//    iso.comap(List("a", "b")) shouldBe List(C("a"), C("b"))
//  }
//
//  test("Case class isomorphism implies seq to list isomorphism") {
//    val iso = implicitly[Isomorphism[Seq[C], List[String]]]
//    iso.map(Seq(C("a"), C("b"))) shouldBe List("a", "b")
//    iso.comap(List("a", "b")) shouldBe Seq(C("a"), C("b"))
//  }
//
//  import java.time.YearMonth
//
//  test("List[Obj[String]] <-> List[String]") {
//    "val iso = implicitly[Isomorphism[List[YearMonth], List[String]]]" should compile
//  }
//
//  test("Seq[Obj[String]] <-> List[String]") {
//    "val iso = implicitly[Isomorphism[Seq[YearMonth], List[String]]]" should compile
//  }
//}
