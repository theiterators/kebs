import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}
import pl.iterators.kebs.enumeratum.ValueEnumOf
import pl.iterators.kebs.enums.ValueEnumLikeEntry
import enumeratum.values._

object ValueEnumTest extends Properties("Deriving") {
  sealed abstract class LibraryItem(val value: Int) extends IntEnumEntry
  object LibraryItem extends IntEnum[LibraryItem] {
    case object Book extends LibraryItem(value = 1)
    case object Movie extends LibraryItem(value = 2)
    case object Magazine extends LibraryItem(3)
    case object CD extends LibraryItem(4)
    val values = findValues
  }

  property("ValueEnumOf derives properly for a value enum") = forAll(Gen.oneOf(LibraryItem.values.toList)) { (libraryItem: LibraryItem) =>
    val tc: ValueEnumOf[Int, ValueEnumLikeEntry[Int]] = implicitly[ValueEnumOf[Int, ValueEnumLikeEntry[Int]]]
    tc.`enum`.values.contains(libraryItem) && tc.`enum`.valueOf(libraryItem.value) == libraryItem
  }
}
