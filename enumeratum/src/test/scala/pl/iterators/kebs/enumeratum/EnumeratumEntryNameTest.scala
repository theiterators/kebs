package pl.iterators.kebs.enumeratum

import enumeratum._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.enums.EnumLike

object EnumeratumEntryNameTest {
  sealed trait Status extends EnumEntry
  object Status       extends Enum[Status] {
    case object Active   extends Status { override val entryName = "is-active" }
    case object Inactive extends Status
    case object Blocked  extends Status
    case object Deleted  extends Status
    case object Pending  extends Status
    case object Archived extends Status
    val values = findValues
  }

  sealed trait Kind extends EnumEntry.Uppercase
  object Kind       extends Enum[Kind] {
    case object RideHailing extends Kind
    case object Delivery    extends Kind
    val values = findValues
  }
}

class EnumeratumEntryNameTest extends AnyFunSuite with Matchers with KebsEnumeratum {
  import EnumeratumEntryNameTest._

  private val status = implicitly[EnumLike[Status]]
  private val kind   = implicitly[EnumLike[Kind]]

  test("EnumLike uses entryName as the name") {
    status.getName(Status.Active) shouldBe "is-active"
    status.names shouldBe List("is-active", "Inactive", "Blocked", "Deleted", "Pending", "Archived")
    kind.getName(Kind.RideHailing) shouldBe "RIDEHAILING"
  }

  test("EnumLike looks entries up by entryName") {
    status.withName("is-active") shouldBe Status.Active
    status.withNameOption("Active") shouldBe None
    status.withNameInsensitiveOption("IS-ACTIVE") shouldBe Some(Status.Active)
    status.withNameIgnoreCase("Is-Active") shouldBe Status.Active
    status.withNameIgnoreCaseOption("active") shouldBe None
    status.valueOfIgnoreCase("IS-ACTIVE") shouldBe Status.Active
    kind.withNameUppercaseOnly("RIDEHAILING") shouldBe Kind.RideHailing
    kind.withNameLowercaseOnlyOption("ridehailing") shouldBe Some(Kind.RideHailing)
    an[NoSuchElementException] should be thrownBy status.withNameIgnoreCase("xxx")
  }

  test("EnumLike keeps declaration order") {
    status.values shouldBe Status.values
    status.fromOrdinal(4) shouldBe Status.Pending
    status.indexOf(Status.Archived) shouldBe 5
  }

  test("EnumLike is not derived for a single entry type") {
    "implicitly[EnumLike[Status.Active.type]]" shouldNot compile
  }
}
