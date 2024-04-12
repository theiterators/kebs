package pl.iterators.kebs.slick.arrays

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.instances.time.YearMonthString

class ListIsomorphismTest extends AnyFunSuite with Matchers with YearMonthString {
  import pl.iterators.kebs.slick._
  import pl.iterators.kebs.core.macros.CaseClass1ToValueClass._
  import slick.jdbc.PostgresProfile.api._

  case class C(a: String)

  test("No CaseClass1Rep implicits derived") {

    "implicitly[CaseClass1Rep[YearMonth, String]]" shouldNot typeCheck
    "implicitly[CaseClass1Rep[String, YearMonth]]" shouldNot typeCheck
  }

  test("Case class isomorphism implies list isomorphism") {
    val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[List[C], List[String]]]
    iso.map(List(C("a"), C("b"))) shouldBe List("a", "b")
    iso.comap(List("a", "b")) shouldBe List(C("a"), C("b"))
  }

  test("Case class isomorphism implies seq to list isomorphism") {
    val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Seq[C], List[String]]]
    iso.map(Seq(C("a"), C("b"))) shouldBe List("a", "b")
    iso.comap(List("a", "b")) shouldBe Seq(C("a"), C("b"))
  }

  import java.time.YearMonth

  test("List[Obj[String]] <-> List[String]") {
    "val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[List[YearMonth], List[String]]]" should compile
  }

  test("Seq[Obj[String]] <-> List[String]") {
    "val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Seq[YearMonth], List[String]]]" should compile
  }
}
