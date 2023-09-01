import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.opaque.Opaque
import pl.iterators.kebs.macros.ValueClassLike

object OpaqueTestDomain {
  opaque type TestWrappedInt = Int
  object TestWrappedInt extends Opaque[TestWrappedInt, Int]

  opaque type ValidatedTestWrappedString = String
  object ValidatedTestWrappedString extends Opaque[ValidatedTestWrappedString, String] {
    override def validate(value: String): Either[String, ValidatedTestWrappedString] =
      if (value.isEmpty) Left("Empty string") else Right(value.trim)
  }

  extension (s: ValidatedTestWrappedString) {
    def myMap(f: Char => Char): ValidatedTestWrappedString = s.map(f)
  }
}

object OpaqueTestTypeclass {
  trait Showable[A] {
    def show(a: A): String
  }

  given Showable[Int] = (a: Int) => a.toString
  given[S, A](using showable: Showable[S], cc1Rep: ValueClassLike[A, S]): Showable[A] = (a: A) => showable.show(cc1Rep.unapply(a))
}

class OpaqueTest extends AnyFunSuite with Matchers {
  import OpaqueTestDomain._
  test("Equality") {
    TestWrappedInt(42) shouldEqual TestWrappedInt(42)
    TestWrappedInt(42) shouldNot equal (TestWrappedInt(1337))
    """TestWrappedString("foo") == "foo"""" shouldNot compile
    """implicitly[=:=[TestWrappedString, String]]""" shouldNot compile
  }

  test("Basic ops") {
    TestWrappedInt(42).unwrap shouldEqual 42
    TestWrappedInt.from(42) should equal (Right(TestWrappedInt(42)))
  }

  test("Validation & sanitization") {
    an[IllegalArgumentException] should be thrownBy ValidatedTestWrappedString("")
    ValidatedTestWrappedString.unsafe("").unwrap should equal ("")
    ValidatedTestWrappedString(" foo ").unwrap should equal ("foo")
    ValidatedTestWrappedString.from("") should equal (Left("Empty string"))
    ValidatedTestWrappedString.from(" foo ") should equal (Right(ValidatedTestWrappedString("foo")))
  }

  test("Extension") {
    ValidatedTestWrappedString("foo").myMap(_.toUpper) shouldEqual ValidatedTestWrappedString("FOO")
  }

  test("Basic derivation") {
    "implicitly[CaseClass1Rep[ValidatedTestWrappedString, String]]" should compile
    implicitly[ValueClassLike[ValidatedTestWrappedString, String]].apply("foo") shouldEqual ValidatedTestWrappedString("foo")
    implicitly[ValueClassLike[ValidatedTestWrappedString, String]].unapply(ValidatedTestWrappedString("foo")) shouldEqual "foo"
    an[IllegalArgumentException] should be thrownBy implicitly[ValueClassLike[ValidatedTestWrappedString, String]].apply("")
  }

  test("Typeclass derivation") {
    import OpaqueTestTypeclass._
    "implicitly[Showable[TestWrappedInt]]" should compile
    implicitly[Showable[TestWrappedInt]].show(TestWrappedInt(42)) shouldEqual "42"
  }
}
