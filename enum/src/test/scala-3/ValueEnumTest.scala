import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}
import pl.iterators.kebs.enums.ValueEnumOf

object DerivingSpecification extends Properties("Deriving") {

  enum ColorButRGB(val value: Int) {
    case Red extends ColorButRGB(0xFF0000)
    case Green extends ColorButRGB(0x00FF00)
    case Blue extends ColorButRGB(0x0000FF)
  }

  property("ValueEnumOf derives properly for an enum") = forAll(Gen.oneOf(ColorButRGB.values.toList)) { (color: ColorButRGB) =>
    val tc = implicitly[ValueEnumOf[Int, ColorButRGB]]
    tc.`enum`.values.contains(color) && tc.`enum`.valueOf(color.value) == color && tc.`enum`.fromOrdinal(color.ordinal) == color
  }
}
