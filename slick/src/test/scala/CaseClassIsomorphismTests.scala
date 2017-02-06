import org.scalatest.{FunSuite, Matchers}
import slick.lifted.Isomorphism

class CaseClassIsomorphismTests extends FunSuite with Matchers {
  import pl.iterators.kebs._

  case class Simple1(a: Int)
  case class Simple2(a: Option[Int])

  test("implicit isomorphism from case class of arity 1") {
    val iso = implicitly[Isomorphism[Simple1, Int]]
    iso.map(Simple1(10)) shouldBe 10
    iso.comap(10) shouldBe Simple1(10)
  }

  test("implicit isomorphism from case class of arity 1 - parametrized return type") {
    val iso = implicitly[Isomorphism[Simple2, Option[Int]]]
    iso.map(Simple2(Some(10))) shouldBe Some(10)
    iso.comap(Some(10)) shouldBe Simple2(Some(10))
  }

  case class TooBig(a: Int, b: Int)

  test("no isomorphism for case classes of arity > 1") {
    "implicitly[Isomorphism[TooBig, _]]" shouldNot compile
  }

  case object NoIsoForYou

  test("no isomorphism for case classes of arity == 0") {
    "implicitly[Isomorphism[NoIsoForYou.type, _]]" shouldNot compile
  }

  class JustAClass(val a: Int)

  test("no isomorhpsim for ordinary classes") {
    "implicitly[Isomorphism[JustAClass, Int]]" shouldNot compile
  }

  case class Parametrized[P](a: P)

  test("implicit isomorphism from parametrized case class of arity 1") {
    val iso = implicitly[Isomorphism[Parametrized[Int], Int]]
    iso.map(Parametrized(10)) shouldBe 10
    iso.comap(10) shouldBe Parametrized(10)
  }

  test("implicit isomorphism from parametrized case class of arity 1 - unreified type parameter") {
    def iso[P]: Isomorphism[Parametrized[P], P] = implicitly[Isomorphism[Parametrized[P], P]]
    iso[Int].map(Parametrized(10)) shouldBe 10
    iso[Option[Int]].comap(Some(10)) shouldBe Parametrized(Some(10))
  }
}
