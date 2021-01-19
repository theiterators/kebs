package pl.iterators.kebs.scalacheck

import org.scalacheck.ScalacheckShapeless._
import enumeratum.scalacheck._
import org.scalacheck.{Arbitrary, Gen}
import pl.iterators.kebs.macros.CaseClass1Rep

import scala.util.Random

trait CommonArbitrarySupport {
  implicit val arbString: Arbitrary[String] = Arbitrary(Gen.delay(Random.alphanumeric.take(20).mkString))

  implicit def caseClass1RepJsonSchemaPredef[T, A](implicit rep: CaseClass1Rep[T, A], arbitrary: Arbitrary[A]): Arbitrary[T] =
    arbitrary.asInstanceOf[Arbitrary[T]]
}

trait MinimalArbitrarySupport {
  implicit def emptyOption[T: Arbitrary]: Arbitrary[Option[T]] = Arbitrary(Gen.const(Option.empty[T]))

  implicit def emptyList[T: Arbitrary]: Arbitrary[List[T]] = Arbitrary(Gen.listOfN(0, Arbitrary.arbitrary[T]))
}

trait MaximalArbitrarySupport {
  implicit def someOption[T: Arbitrary]: Arbitrary[Option[T]] = Arbitrary(Gen.some(Arbitrary.arbitrary[T]))

  implicit def nonEmptyList[T: Arbitrary]: Arbitrary[List[T]] = Arbitrary(Gen.listOfN(1 + Random.nextInt(3), Arbitrary.arbitrary[T]))
}

trait Generator[T] extends CommonArbitrarySupport {
  def ArbT: Arbitrary[T]

  def generate: T = ArbT.arbitrary.sample.get
}

trait MinimalGenerator[T] extends CommonArbitrarySupport with MinimalArbitrarySupport {
  def ArbT: Arbitrary[T]

  def generate: T = ArbT.arbitrary.sample.get
}

trait MaximalGenerator[T] extends CommonArbitrarySupport with MaximalArbitrarySupport {
  def ArbT: Arbitrary[T]

  def generate: T = ArbT.arbitrary.sample.get
}

trait AllGenerators[T] {

  val normal: Generator[T]

  val minimal: MinimalGenerator[T]

  val maximal: MaximalGenerator[T]
}
