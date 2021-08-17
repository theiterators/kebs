package caseclasses

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class CaseClassIsomorphismTests extends AnyFunSuite with Matchers {
  import slick.lifted.Isomorphism
  import pl.iterators.kebs._

  case class Simple1(a: Int)
  case class Simple2(a: Option[Int])

  test("Implicit isomorphism for case class of arity 1") {
    val iso = implicitly[Isomorphism[Simple1, Int]]
    iso.map(Simple1(10)) shouldBe 10
    iso.comap(10) shouldBe Simple1(10)
  }

  test("Implicit isomorphism for case class of arity 1 - parametrized return type") {
    val iso = implicitly[Isomorphism[Simple2, Option[Int]]]
    iso.map(Simple2(Some(10))) shouldBe Some(10)
    iso.comap(Some(10)) shouldBe Simple2(Some(10))
  }

  case class TooBig(a: Int, b: Int)

  test("No isomorphism for case classes of arity > 1") {
    "implicitly[Isomorphism[TooBig, _]]" shouldNot typeCheck
  }

  case object NoIsoForYou

  test("No isomorphism for case classes of arity == 0") {
    "implicitly[Isomorphism[NoIsoForYou.type, _]]" shouldNot typeCheck
  }

  class JustAClass(val a: Int)

  test("No isomorphism for ordinary classes") {
    "implicitly[Isomorphism[JustAClass, Int]]" shouldNot typeCheck
  }

  case class Parametrized[P](a: P)

  test("Implicit isomorphism for parametrized case class of arity 1") {
    val iso = implicitly[Isomorphism[Parametrized[Int], Int]]
    iso.map(Parametrized(10)) shouldBe 10
    iso.comap(10) shouldBe Parametrized(10)
  }

  test("Implicit isomorphism for parametrized case class of arity 1 - unrefined type parameter") {
    def iso[P]: Isomorphism[Parametrized[P], P] = implicitly[Isomorphism[Parametrized[P], P]]
    iso[Int].map(Parametrized(10)) shouldBe 10
    iso[Option[Int]].comap(Some(10)) shouldBe Parametrized(Some(10))
  }
}
