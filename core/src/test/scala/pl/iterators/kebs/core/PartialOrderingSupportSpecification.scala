package pl.iterators.kebs.core

import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}

object PartialOrderingSupportSpecification extends Properties("PartialOrderingSupport") {

  import StringsDomain._
  import support._

  private def maybeCompare[A](e1: A, e2: A)(implicit PO: PartialOrdering[A]): Option[Int] =
    PO.tryCompare(e1, e2)

  implicit private val partialOrdering: PartialOrdering[String] = new PartialOrdering[String] {
    private val maxLength                                      = 10
    override def tryCompare(x: String, y: String): Option[Int] = (x.length, y.length) match {
      case (lx, ly) if (lx < maxLength && ly < maxLength) => Some(x.compareTo(y))
      case _                                              => None
    }
    override def lteq(x: String, y: String): Boolean = (x.length, y.length) match {
      case (lx, ly) if (lx < maxLength && ly < maxLength) => x < y
      case _                                              => false
    }
  }

  private val stringLessThan10Length = for {
    n <- Gen.choose(0, 9)
    l <- Gen.listOfN(n, Gen.asciiChar)
  } yield l.mkString

  private val stringAtLeast10Length = for {
    n <- Gen.choose(10, 1000)
    l <- Gen.listOfN(n, Gen.asciiChar)
  } yield l.mkString

  property("partial ordering should be defined for short tagged strings") = forAll(stringLessThan10Length, stringLessThan10Length) {
    (shortString1, shortString2) =>
      maybeCompare(TaggedString(shortString1), TaggedString(shortString2)).isDefined
  }
  property("partial ordering should not be defined for short and long tagged strings") =
    forAll(stringLessThan10Length, stringAtLeast10Length) { (shortString, longString) =>
      maybeCompare(TaggedString(shortString), TaggedString(longString)).isEmpty
    }
  property("partial ordering should not be defined for long and short tagged strings") =
    forAll(stringAtLeast10Length, stringLessThan10Length) { (longString, shortString) =>
      maybeCompare(TaggedString(longString), TaggedString(shortString)).isEmpty
    }
  property("partial ordering should not be defined for long and short tagged strings") =
    forAll(stringAtLeast10Length, stringAtLeast10Length) { (longString1, longString2) =>
      maybeCompare(TaggedString(longString1), TaggedString(longString2)).isEmpty
    }

  property("partial ordering should be defined for short boxed strings") = forAll(stringLessThan10Length, stringLessThan10Length) {
    (shortString1, shortString2) =>
      maybeCompare(BoxedString(shortString1), BoxedString(shortString2)).isDefined
  }
  property("partial ordering should not be defined for short and long boxed strings") =
    forAll(stringLessThan10Length, stringAtLeast10Length) { (shortString, longString) =>
      maybeCompare(BoxedString(shortString), BoxedString(longString)).isEmpty
    }
  property("partial ordering should not be defined for long and short boxed strings") =
    forAll(stringAtLeast10Length, stringLessThan10Length) { (longString, shortString) =>
      maybeCompare(BoxedString(longString), BoxedString(shortString)).isEmpty
    }
  property("partial ordering should not be defined for long and short boxed strings") =
    forAll(stringAtLeast10Length, stringAtLeast10Length) { (longString1, longString2) =>
      maybeCompare(BoxedString(longString1), BoxedString(longString2)).isEmpty
    }
}
