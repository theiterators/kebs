package pl.iterators.kebs.enumeratum

import enumeratum.values._
import enumeratum.{Enum, EnumEntry}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.enums.{EnumLike, ValueEnumLike, ValueEnumLikeEntry}

object EnumeratumCompanionTestDomain {
  sealed trait Partial extends EnumEntry
  object Partial       extends Enum[Partial] {
    case object A      extends Partial
    case object B      extends Partial
    case object Hidden extends Partial
    val values = IndexedSeq(A, B)
  }

  sealed abstract class PartialLevel(val value: Int) extends IntEnumEntry with ValueEnumLikeEntry[Int]
  object PartialLevel                                extends IntEnum[PartialLevel] {
    case object Low    extends PartialLevel(1)
    case object High   extends PartialLevel(2)
    case object Secret extends PartialLevel(3)
    val values = IndexedSeq(Low, High)
  }

  sealed abstract class ShortLevel(val value: Short) extends ShortEnumEntry with ValueEnumLikeEntry[Short]
  object ShortLevel                                  extends ShortEnum[ShortLevel] {
    case object One extends ShortLevel(1)
    case object Two extends ShortLevel(2)
    val values = findValues
  }

  sealed abstract class ByteLevel(val value: Byte) extends ByteEnumEntry with ValueEnumLikeEntry[Byte]
  object ByteLevel                                 extends ByteEnum[ByteLevel] {
    case object One extends ByteLevel(1)
    val values = findValues
  }

  sealed abstract class Code(val value: String) extends StringEnumEntry with ValueEnumLikeEntry[String]
  object Code                                   extends StringEnum[Code] {
    case object Ok  extends Code("ok")
    case object Err extends Code("err")
    val values = findValues
  }
}

class EnumeratumCompanionTests extends AnyFunSuite with Matchers with KebsEnumeratum with KebsValueEnumeratum {
  import EnumeratumCompanionTestDomain._

  test("respects a user-defined values list") {
    implicitly[EnumLike[Partial]].values shouldBe List(Partial.A, Partial.B)
    implicitly[EnumLike[Partial]].withNameOption("Hidden") shouldBe None
  }

  test("value enum respects a user-defined values list") {
    implicitly[ValueEnumLike[Int, PartialLevel]].values shouldBe List(PartialLevel.Low, PartialLevel.High)
  }

  test("Short, Byte and String value enums") {
    implicitly[ValueEnumLike[Short, ShortLevel]].valueOf(2.toShort) shouldBe ShortLevel.Two
    implicitly[ValueEnumLike[Byte, ByteLevel]].valueOf(1.toByte) shouldBe ByteLevel.One
    implicitly[ValueEnumLike[String, Code]].valueOf("err") shouldBe Code.Err
  }
}
