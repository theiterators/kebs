import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}
import pl.iterators.kebs.enums.EnumOf

object EnumTest extends Properties("Deriving") {
  enum Color {
    case Red, Green, Blue
  }

  property("EnumOf derives properly for an enum") = forAll(Gen.oneOf(Color.values.toList)) { (color: Color) =>
    val tc = implicitly[EnumOf[Color]]
    tc.`enum`.values.contains(color) && tc.`enum`.valueOf(color.toString) == color && tc.`enum`.fromOrdinal(color.ordinal) == color
  }
}
