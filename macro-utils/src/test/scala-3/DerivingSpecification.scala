import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}

import pl.iterators.kebs.macros.CaseClass1Rep

object DerivingSpecification extends Properties("CaseClass1Rep") {

  import pl.iterators.kebs.support._

  case class CC1Ex(whatever: String)

  property("caseClass1Rep dervies properly from 1-element case class") = forAll { (stringValue: String) =>
    val tc = implicitly[CaseClass1Rep[CC1Ex, String]]
    tc.apply(stringValue) == CC1Ex(stringValue) && tc.unapply(CC1Ex(stringValue)) == stringValue
  }
}
