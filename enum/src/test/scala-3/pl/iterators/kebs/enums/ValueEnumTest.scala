package pl.iterators.kebs.enums

import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}
import scala.deriving.Mirror
import scala.reflect.{ClassTag, Enum}

import pl.iterators.kebs.core.enums.{ValueEnumLike, ValueEnumLikeEntry}

object DerivingSpecification extends Properties("Deriving") with KebsValueEnum {

  enum ColorButRGB(val value: Int) extends ValueEnumLikeEntry[Int] {
    case Red extends ColorButRGB(0xFF0000)
    case Green extends ColorButRGB(0x00FF00)
    case Blue extends ColorButRGB(0x0000FF)
  }

  property("ValueEnumLike derives properly for a value enum") = forAll(Gen.oneOf(ColorButRGB.values.toList)) { (color: ColorButRGB) =>
    val tc = implicitly[ValueEnumLike[Int, ColorButRGB]]
    tc.values.contains(color) && tc.valueOf(color.value) == color && tc.fromOrdinal(color.ordinal) == color
  }
}
