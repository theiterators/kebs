package pl.iterators.kebs.core

import org.scalacheck.Prop.forAll
import org.scalacheck.Properties
import pl.iterators.kebs.core.macros.{CaseClass1ToValueClass, ValueClassLike}

object DerivingSpecification extends Properties("Deriving") with CaseClass1ToValueClass {
  case class CC1Ex(whatever: String)

  property("ValueClassLike derives properly from 1-element case class") = forAll { (stringValue: String) =>
    val tc = implicitly[ValueClassLike[CC1Ex, String]]
    tc.apply(stringValue) == CC1Ex(stringValue) && tc.unapply(CC1Ex(stringValue)) == stringValue
  }
}
