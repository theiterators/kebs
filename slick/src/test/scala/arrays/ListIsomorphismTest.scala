package arrays

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.instances.TimeInstances.YearMonthString
import slick.lifted.Isomorphism

class ListIsomorphismTest extends AnyFunSuite with Matchers with YearMonthString {
  import pl.iterators.kebs._

  case class C(a: String)

  test("Case class isomorphism implies list isomorphism") {
    val iso = implicitly[Isomorphism[List[C], List[String]]]
    iso.map(List(C("a"), C("b"))) shouldBe List("a", "b")
    iso.comap(List("a", "b")) shouldBe List(C("a"), C("b"))
  }

  test("Case class isomorphism implies seq to list isomorphism") {
    val iso = implicitly[Isomorphism[Seq[C], List[String]]]
    iso.map(Seq(C("a"), C("b"))) shouldBe List("a", "b")
    iso.comap(List("a", "b")) shouldBe Seq(C("a"), C("b"))
  }

  import java.time.YearMonth

  test("Year month isomorphism implies seq to list isomorphism") {
    val iso = implicitly[Isomorphism[List[YearMonth], List[String]]]
    iso.map(List(YearMonth.of(2021, 5), YearMonth.of(2020, 4))) shouldBe List("2021-05", "2020-04")
    iso.comap(List("2021-05", "2020-04")) shouldBe List(YearMonth.of(2021, 5), YearMonth.of(2020, 4))
  }
}
