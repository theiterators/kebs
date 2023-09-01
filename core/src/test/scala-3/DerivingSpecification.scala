import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}
import pl.iterators.kebs.macros.ValueClassLike
import pl.iterators.kebs.macros.enums.{EnumOf, ValueEnumOf}
import pl.iterators.kebs.enums.ValueEnum

object DerivingSpecification extends Properties("Deriving") {
  case class CC1Ex(whatever: String)

  property("CaseClass1Rep derives properly from 1-element case class") = forAll { (stringValue: String) =>
    val tc = implicitly[ValueClassLike[CC1Ex, String]]
    tc.apply(stringValue) == CC1Ex(stringValue) && tc.unapply(CC1Ex(stringValue)) == stringValue
  }

  enum Color {
    case Red, Green, Blue
  }

  property("EnumOf derives properly for an enum") = forAll(Gen.oneOf(Color.values.toList)) { (color: Color) =>
    val tc = implicitly[EnumOf[Color]]
    tc.`enum`.values.contains(color) && tc.`enum`.valueOf(color.toString) == color && tc.`enum`.fromOrdinal(color.ordinal) == color
  }

  enum ColorButRGB(val value: Int) extends ValueEnum[Int] {
    case Red extends ColorButRGB(0xFF0000)
    case Green extends ColorButRGB(0x00FF00)
    case Blue extends ColorButRGB(0x0000FF)
  }

  property("ValueEnumOf derives properly for an enum") = forAll(Gen.oneOf(ColorButRGB.values.toList)) { (color: ColorButRGB) =>
    val tc = implicitly[ValueEnumOf[Int, ColorButRGB]]
    tc.`enum`.values.contains(color) && tc.`enum`.valueOf(color.value) == color && tc.`enum`.fromOrdinal(color.ordinal) == color
  }
}
