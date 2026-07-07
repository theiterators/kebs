package pl.iterators.kebs.jsoniter.formats

import com.github.plokhotnyuk.jsoniter_scala.core._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.instances.net.URIString
import pl.iterators.kebs.jsoniter.{KebsJsoniter, KebsJsoniterCapitalized, KebsJsoniterSnakified}
import pl.iterators.kebs.opaque.Opaque

import java.net.URI
import java.time.Instant
import java.util.UUID

opaque type UserId = UUID
object UserId extends Opaque[UserId, UUID]

opaque type UserName <: String = String
object UserName extends Opaque[UserName, String]

opaque type CreatedAt <: Instant = Instant
object CreatedAt extends Opaque[CreatedAt, Instant]

object DeriveCodecProtocol            extends KebsJsoniter with URIString
object SnakifiedDeriveCodecProtocol   extends KebsJsoniterSnakified
object CapitalizedDeriveCodecProtocol extends KebsJsoniterCapitalized

final case class User(id: UserId, name: Option[UserName], createdAt: CreatedAt, homepage: URI)
object User {
  // URI's codec is only reachable via URIString's InstanceConverter, which is a member of DeriveCodecProtocol
  // (not of deriveCodec's own inline body, unlike the flatCodec path for opaque types above); jsoniter's macro-time
  // implicit search only sees it when it is imported into scope, matching the established convention in every other
  // kebs-jsoniter test file (`object Protocol extends KebsJsoniter with X; import Protocol._`).
  import DeriveCodecProtocol._
  implicit val codec: JsonValueCodec[User]         = deriveCodec
  implicit val seqCodec: JsonValueCodec[Seq[User]] = deriveCodec
}

final case class RenamedFields(firstName: UserName, lastActiveAt: CreatedAt)
object RenamedFields {
  implicit val codec: JsonValueCodec[RenamedFields] = SnakifiedDeriveCodecProtocol.deriveCodec
}

final case class PascalFields(firstName: UserName)
object PascalFields {
  implicit val codec: JsonValueCodec[PascalFields] = CapitalizedDeriveCodecProtocol.deriveCodec
}

final case class NullableFields(count: Int, label: String)
object NullableFields {
  implicit val codec: JsonValueCodec[NullableFields] = DeriveCodecProtocol.deriveCodec
}

class DeriveCodecScala3Tests extends AnyFunSuite with Matchers {

  val user = User(
    UserId(UUID.fromString("00000000-0000-0000-0000-000000000001")),
    Some(UserName("jan")),
    CreatedAt(Instant.parse("2026-01-01T00:00:00Z")),
    new URI("https://iterators.pl")
  )
  val userJson =
    "{\"id\":\"00000000-0000-0000-0000-000000000001\",\"name\":\"jan\",\"createdAt\":\"2026-01-01T00:00:00Z\",\"homepage\":\"https://iterators.pl\"}"

  test("case class of opaque types flattens to underlying representations and round-trips") {
    writeToString(user) shouldBe userJson
    readFromString[User](userJson) shouldBe user
  }

  test("InstanceConverter field (URI) is serialized as a string") {
    writeToString(user) should include("\"homepage\":\"https://iterators.pl\"")
  }

  test("None field is written as null (withTransientNone(false))") {
    writeToString(user.copy(name = None)) should include("\"name\":null")
  }

  test("Seq at the top level derives the exact wire shape") {
    writeToString(Seq(user)) shouldBe s"[$userJson]"
    readFromString[Seq[User]](s"[$userJson]") shouldBe Seq(user)
  }

  test("snakified flavor") {
    val value = RenamedFields(UserName("jan"), CreatedAt(Instant.parse("2026-01-01T00:00:00Z")))
    writeToString(value) shouldBe "{\"first_name\":\"jan\",\"last_active_at\":\"2026-01-01T00:00:00Z\"}"
    readFromString[RenamedFields]("{\"first_name\":\"jan\",\"last_active_at\":\"2026-01-01T00:00:00Z\"}") shouldBe value
  }

  test("capitalized flavor") {
    writeToString(PascalFields(UserName("jan"))) shouldBe "{\"FirstName\":\"jan\"}"
  }

  test("deriveCodec on a kebs type is a guarded compile error") {
    assertDoesNotCompile("DeriveCodecProtocol.deriveCodec[UserId]")
  }

  test("null in a plain reference field is encoded as JSON null, not an NPE") {
    writeToString(NullableFields(10, null)) shouldBe "{\"count\":10,\"label\":null}"
  }
}
