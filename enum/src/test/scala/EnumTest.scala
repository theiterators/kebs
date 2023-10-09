import domain.ColorDomain
import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}
import pl.iterators.kebs.enums.{EnumLike, KebsEnum}
import scala.collection.immutable

object EnumTest extends Properties("Deriving") with KebsEnum {

  property("EnumLike derives properly for an enum") = forAll(Gen.oneOf(ColorDomain.colorValues)) { (color: ColorDomain.colorType) =>
    val tc = implicitly[EnumLike[ColorDomain.colorType]]
    tc.values.contains(color) && tc.valueOf(color.toString) == color
  }
}
