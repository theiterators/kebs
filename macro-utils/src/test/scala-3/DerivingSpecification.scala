import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}
import pl.iterators.kebs.macros.base.CaseClass1Rep
import pl.iterators.kebs.macros.enums.EnumOf

object DerivingSpecification extends Properties("Deriving") {
  case class CC1Ex(whatever: String)

  property("CaseClass1Rep derives properly from 1-element case class") = forAll { (stringValue: String) =>
    val tc = implicitly[CaseClass1Rep[CC1Ex, String]]
    tc.apply(stringValue) == CC1Ex(stringValue) && tc.unapply(CC1Ex(stringValue)) == stringValue
  }

  enum Color {
    case Red, Green, Blue
  }

  property("EnumOf derives properly for an enum") = forAll(Gen.oneOf(Color.values.toList)) { (color: Color) =>
    val tc = implicitly[EnumOf[Color]]
    tc.`enum`.values.contains(color) && tc.`enum`.valueOf(color.toString) == color && tc.`enum`.fromOrdinal(color.ordinal) == color
  }
}
