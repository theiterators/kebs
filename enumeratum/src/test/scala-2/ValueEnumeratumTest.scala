import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import enumeratum.values._
import pl.iterators.kebs.enumeratum._
import pl.iterators.kebs.enums.ValueEnumLikeEntry


sealed abstract class LibraryItem(val value: Int) extends IntEnumEntry
object LibraryItem extends IntEnum[LibraryItem] {
  case object Book     extends LibraryItem(value = 1)
  case object Movie    extends LibraryItem(value = 2)
  case object Magazine extends LibraryItem(3)
  case object CD       extends LibraryItem(4)

  val values = findValues
}

class MyValueEnumTest extends AnyFunSuite with Matchers {

  val valueEnumOf: ValueEnumOf[Int, ValueEnumLikeEntry[Int]] = implicitly[ValueEnumOf[Int, ValueEnumLikeEntry[Int]]]

  val enumEntrySeq: Seq[ValueEnumLikeEntry[Int]] = LibraryItem.values.map(item =>
    new ValueEnumLikeEntry[Int] {
      override def value: Int = item.value
  })
  println(enumEntrySeq)

  test("ValueEnumOf[Int, IntEnumEntry].valueEnum.values should return all values of IntEnum") {
    valueEnumOf.valueEnum.values should contain theSameElementsAs Seq(LibraryItem.Book, LibraryItem.Movie, LibraryItem.Magazine, LibraryItem.CD)
  }
}
