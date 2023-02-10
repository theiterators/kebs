import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}
import pl.iterators.kebs.macros.CaseClass1Rep

object DerivingSpecification extends Properties("Deriving") {
  case class CC1Ex(whatever: String)

  property("CaseClass1Rep derives properly from 1-element case class") = forAll { (stringValue: String) =>
    val tc = implicitly[CaseClass1Rep[CC1Ex, String]]
    tc.apply(stringValue) == CC1Ex(stringValue) && tc.unapply(CC1Ex(stringValue)) == stringValue
  }
}
