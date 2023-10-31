package pl.iterators.kebs.scalacheck

import enumeratum.{ScalacheckInstances => EnumScalacheckInstances}
import org.scalacheck.Arbitrary
import pl.iterators.kebs.macros.CaseClass1Rep

trait CommonArbitrarySupport extends EnumScalacheckInstances with ScalacheckInstancesSupport {
  
  implicit def caseClass1RepArbitraryPredef[T, A](
      implicit rep: CaseClass1Rep[T, A],
      arb: Arbitrary[A]
  ): Arbitrary[T] = Arbitrary(arb.arbitrary.map(rep.apply(_)))
  
}
