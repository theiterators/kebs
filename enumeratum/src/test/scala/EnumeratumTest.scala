import enumeratum._
import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}
import pl.iterators.kebs.enumeratum.KebsEnumeratum
import pl.iterators.kebs.enums.EnumLike

object EnumeratumTest extends Properties("Deriving") with KebsEnumeratum {

  sealed trait Greeting extends EnumEntry
  object Greeting extends Enum[Greeting] {
    val values = findValues
    case object Hello extends Greeting
    case object GoodBye extends Greeting
    case object Hi extends Greeting
    case object Bye extends Greeting
  }

  property("EnumLike derives properly for an enumeratum enum") = forAll(Gen.oneOf(Greeting.values.toList)) { (greeting: Greeting) =>
    val tc = implicitly[EnumLike[Greeting]]
    tc.values.contains(greeting) && tc.valueOf(greeting.toString) == greeting && tc.fromOrdinal(Greeting.values.indexOf(greeting)) == greeting
  }
}
