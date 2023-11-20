import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}
import pl.iterators.kebs.macros.ValueClassLike

object DerivingSpecification extends Properties("Deriving") {
  case class CC1Ex(whatever: String)

  property("ValueClassLike derives properly from 1-element case class") = forAll { (stringValue: String) =>
    val tc = implicitly[ValueClassLike[CC1Ex, String]]
    tc.apply(stringValue) == CC1Ex(stringValue) && tc.unapply(CC1Ex(stringValue)) == stringValue
  }
}
