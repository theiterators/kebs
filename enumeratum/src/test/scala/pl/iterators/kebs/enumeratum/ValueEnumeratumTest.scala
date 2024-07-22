package pl.iterators.kebs.enumeratum

import enumeratum.values._
import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}
import pl.iterators.kebs.core.enums.{ValueEnumLike, ValueEnumLikeEntry}

object ValueEnumTest extends Properties("Deriving") with KebsValueEnumeratum {
  sealed abstract class LibraryItem(val value: Int) extends IntEnumEntry with ValueEnumLikeEntry[Int]
  object LibraryItem extends IntEnum[LibraryItem] {
    case object Book     extends LibraryItem(value = 1)
    case object Movie    extends LibraryItem(value = 2)
    case object Magazine extends LibraryItem(3)
    case object CD       extends LibraryItem(4)
    val values = findValues
  }

  property("ValueEnumLike derives properly for a value enum") = forAll(Gen.oneOf(LibraryItem.values.toList)) {
    (libraryItem: LibraryItem) =>
      val tc = implicitly[ValueEnumLike[Int, LibraryItem]]
      tc.values.contains(libraryItem) && tc.valueOf(libraryItem.value) == libraryItem
  }
}
