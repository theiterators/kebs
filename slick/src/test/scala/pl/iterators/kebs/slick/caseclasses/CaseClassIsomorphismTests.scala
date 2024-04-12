package pl.iterators.kebs.slick.caseclasses

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class CaseClassIsomorphismTests extends AnyFunSuite with Matchers {
  import pl.iterators.kebs.slick._
  import pl.iterators.kebs.core.macros.CaseClass1ToValueClass._
  import slick.jdbc.PostgresProfile.api._

  case class Simple1(a: Int)
  case class Simple2(a: Option[Int])

  test("Implicit isomorphism for case class of arity 1") {
    val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Simple1, Int]]
    iso.map(Simple1(10)) shouldBe 10
    iso.comap(10) shouldBe Simple1(10)
  }

  //  test("Implicit isomorphism for case class of arity 1 - parametrized return type") {
  //    val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Simple2, Option[Int]]]
  //    iso.map(Simple2(Some(10))) shouldBe Some(10)
  //    iso.comap(Some(10)) shouldBe Simple2(Some(10))
  //  }

  case class TooBig(a: Int, b: Int)

  test("No isomorphism for case classes of arity > 1") {
    "implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[TooBig, _]]" shouldNot typeCheck
  }

  case object NoIsoForYou

  test("No isomorphism for case classes of arity == 0") {
    "implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[NoIsoForYou.type, _]]" shouldNot typeCheck
  }

  class JustAClass(val a: Int)

  test("No isomorphism for ordinary classes") {
    "implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[JustAClass, Int]]" shouldNot typeCheck
  }

  case class Parametrized[P](a: P)

  test("Implicit isomorphism for parametrized case class of arity 1") {
    val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Parametrized[Int], Int]]
    iso.map(Parametrized(10)) shouldBe 10
    iso.comap(10) shouldBe Parametrized(10)
  }

  //  test("Implicit isomorphism for parametrized case class of arity 1 - unrefined type parameter") {
  //    def iso[P]: slick.jdbc.JdbcTypesComponent#MappedJdbcType[Parametrized[P], P] = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Parametrized[P], P]]
  //    iso[Int].map(Parametrized(10)) shouldBe 10
  //    iso[Option[Int]].comap(Some(10)) shouldBe Parametrized(Some(10))
  //  }
}
