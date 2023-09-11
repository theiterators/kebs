import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}
import pl.iterators.kebs.enumeratum.EnumOf
import enumeratum._

object EnumeratumTest extends Properties("Deriving") {

  sealed trait Greeting extends EnumEntry
  object Greeting extends Enum[Greeting] {
    val values = findValues
    case object Hello extends Greeting
    case object GoodBye extends Greeting
    case object Hi extends Greeting
    case object Bye extends Greeting
  }

  property("EnumOf derives properly for an enum") = forAll(Gen.oneOf(Greeting.values.toList)) { (greeting: Greeting) =>
    val tc = implicitly[EnumOf[Greeting]]
    tc.`enum`.values.contains(greeting) && tc.`enum`.valueOf(greeting.toString) == greeting && tc.`enum`.fromOrdinal(Greeting.values.indexOf(greeting)) == greeting
  }
}
