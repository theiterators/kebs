package pl.iterators.kebs.jsoniter.formats

import com.github.plokhotnyuk.jsoniter_scala.core._
import pl.iterators.kebs.jsoniter.KebsJsoniter

import java.time._
import java.util.UUID
import scala.collection.immutable.{ListMap, SortedSet}
import scala.collection.mutable

object JsoniterStandardTypesTestDomain {
  final case class Keyed(values: Map[Int, String])
}

class JsoniterStandardTypesTests extends JsoniterTestBase {
  import JsoniterStandardTypesTestDomain._

  object Protocol extends KebsJsoniter
  import Protocol.deriveCodec

  private val uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")

  test("primitives") {
    roundTrip(true, "true")(deriveCodec[Boolean])
    roundTrip(1.toByte, "1")(deriveCodec[Byte])
    roundTrip(1.toShort, "1")(deriveCodec[Short])
    roundTrip(1, "1")(deriveCodec[Int])
    roundTrip(1L, "1")(deriveCodec[Long])
    roundTrip(1.5f, "1.5")(deriveCodec[Float])
    roundTrip(1.5, "1.5")(deriveCodec[Double])
    roundTrip('a', "\"a\"")(deriveCodec[Char])
  }

  test("String, numbers, UUID") {
    roundTrip("żółć \"x\"", "\"żółć \\\"x\\\"\"")(deriveCodec[String])
    roundTrip(BigInt("12345678901234567890"), "12345678901234567890")(deriveCodec[BigInt])
    roundTrip(BigDecimal("1.23"), "1.23")(deriveCodec[BigDecimal])
    val precise = "1234567890123456789012345678901234567890.123"
    roundTrip(BigDecimal(precise, java.math.MathContext.UNLIMITED), precise)(deriveCodec[BigDecimal])
    rejected[BigDecimal]("1" * 400, "")(deriveCodec[BigDecimal])
    rejected[BigDecimal]("1e1000000", "")(deriveCodec[BigDecimal])
    roundTrip(uuid, s"\"$uuid\"")(deriveCodec[UUID])
  }

  test("java.time") {
    roundTrip(Instant.parse("2026-01-02T03:04:05Z"), "\"2026-01-02T03:04:05Z\"")(deriveCodec[Instant])
    roundTrip(LocalDate.of(2026, 1, 2), "\"2026-01-02\"")(deriveCodec[LocalDate])
    roundTrip(LocalTime.of(3, 4, 5), "\"03:04:05\"")(deriveCodec[LocalTime])
    roundTrip(LocalDateTime.of(2026, 1, 2, 3, 4, 5), "\"2026-01-02T03:04:05\"")(deriveCodec[LocalDateTime])
    roundTrip(OffsetDateTime.of(2026, 1, 2, 3, 4, 5, 0, ZoneOffset.ofHours(1)), "\"2026-01-02T03:04:05+01:00\"")(
      deriveCodec[OffsetDateTime]
    )
    roundTrip(OffsetTime.of(3, 4, 5, 0, ZoneOffset.UTC), "\"03:04:05Z\"")(deriveCodec[OffsetTime])
    roundTrip(ZonedDateTime.parse("2026-01-02T03:04:05Z"), "\"2026-01-02T03:04:05Z\"")(deriveCodec[ZonedDateTime])
    roundTrip(Duration.ofSeconds(61), "\"PT1M1S\"")(deriveCodec[Duration])
    roundTrip(Period.ofDays(3), "\"P3D\"")(deriveCodec[Period])
    roundTrip(Year.of(2026), "\"2026\"")(deriveCodec[Year])
    roundTrip(YearMonth.of(2026, 1), "\"2026-01\"")(deriveCodec[YearMonth])
    roundTrip(MonthDay.of(1, 2), "\"--01-02\"")(deriveCodec[MonthDay])
    roundTrip[ZoneId](ZoneOffset.UTC, "\"Z\"")(deriveCodec[ZoneId])
    roundTrip(ZoneOffset.ofHours(2), "\"+02:00\"")(deriveCodec[ZoneOffset])
  }

  test("Option") {
    roundTrip(Option(1), "1")(deriveCodec[Option[Int]])
    roundTrip(Option.empty[Int], "null")(deriveCodec[Option[Int]])
  }

  test("collections") {
    roundTrip(List(1, 2), "[1,2]")(deriveCodec[List[Int]])
    roundTrip(List.empty[Int], "[]")(deriveCodec[List[Int]])
    roundTrip(Vector("a"), "[\"a\"]")(deriveCodec[Vector[String]])
    roundTrip(Seq(1L), "[1]")(deriveCodec[Seq[Long]])
    roundTrip(Set(true), "[true]")(deriveCodec[Set[Boolean]])
    roundTrip(SortedSet(2, 1), "[1,2]")(deriveCodec[SortedSet[Int]])
    roundTrip(mutable.ArrayBuffer(1, 2), "[1,2]")(deriveCodec[mutable.ArrayBuffer[Int]])
    roundTrip(Iterable(1, 2), "[1,2]")(deriveCodec[Iterable[Int]])
  }

  test("arrays") {
    implicit val codec: JsonValueCodec[Array[Int]] = deriveCodec[Array[Int]]
    writeToString(Array(1, 2)) shouldBe "[1,2]"
    readFromString[Array[Int]]("[1,2]").toList shouldBe List(1, 2)
  }

  test("maps") {
    roundTrip(Map("a" -> 1), """{"a":1}""")(deriveCodec[Map[String, Int]])
    roundTrip(Map.empty[String, Int], "{}")(deriveCodec[Map[String, Int]])
    roundTrip(Map(1 -> "a"), """{"1":"a"}""")(deriveCodec[Map[Int, String]])
    roundTrip(Map(uuid -> 1), s"""{"$uuid":1}""")(deriveCodec[Map[UUID, Int]])
    roundTrip(Map(LocalDate.of(2026, 1, 2) -> 1), """{"2026-01-02":1}""")(deriveCodec[Map[LocalDate, Int]])
    roundTrip(Map(true -> 1), """{"true":1}""")(deriveCodec[Map[Boolean, Int]])
    roundTrip(ListMap("b" -> 1, "a" -> 2), """{"b":1,"a":2}""")(deriveCodec[ListMap[String, Int]])
    roundTrip(Keyed(Map(1 -> "a")), """{"values":{"1":"a"}}""")(deriveCodec[Keyed])
  }

  test("a map of any size") {
    val large                                         = (1 to 5000).map(i => i -> i).toMap
    implicit val codec: JsonValueCodec[Map[Int, Int]] = deriveCodec[Map[Int, Int]]
    readFromString[Map[Int, Int]](writeToString(large)) shouldBe large
  }

  test("a user's key codec") {
    implicit val keys: JsonKeyCodec[Int] = new JsonKeyCodec[Int] {
      def decodeKey(in: JsonReader): Int           = in.readKeyAsString().drop(1).toInt
      def encodeKey(x: Int, out: JsonWriter): Unit = out.writeKey("k" + x)
    }
    roundTrip(Map(1 -> "a"), """{"k1":"a"}""")(deriveCodec[Map[Int, String]])
  }

  test("tuples") {
    roundTrip((1, "a"), """[1,"a"]""")(deriveCodec[(Int, String)])
    roundTrip((1, 2, 3), "[1,2,3]")(deriveCodec[(Int, Int, Int)])
    rejected[(Int, String)]("[1]", "")(deriveCodec[(Int, String)])
    rejected[(Int, String)]("""[1,"a",2]""", "")(deriveCodec[(Int, String)])
  }

  test("nested") {
    roundTrip(List(Some(Map("a" -> ((1, true)))), None), """[{"a":[1,true]},null]""")(
      deriveCodec[List[Option[Map[String, (Int, Boolean)]]]]
    )
  }

  test("null is not a value") {
    rejected[String]("null", "")(deriveCodec[String])
    rejected[List[Int]]("null", "")(deriveCodec[List[Int]])
    rejected[Map[String, Int]]("null", "")(deriveCodec[Map[String, Int]])
    rejected[UUID]("null", "")(deriveCodec[UUID])
    rejected[Instant]("null", "")(deriveCodec[Instant])
    rejected[BigDecimal]("null", "")(deriveCodec[BigDecimal])
    rejected[Array[Int]]("null", "")(deriveCodec[Array[Int]])
  }

  test("what is not supported") {
    "deriveCodec[Either[Int, String]]" shouldNot compile
    "deriveCodec[scala.collection.immutable.BitSet]" shouldNot compile
    "deriveCodec[java.lang.Thread]" shouldNot compile
    "deriveCodec[Any]" shouldNot compile
    "deriveCodec[Map[java.lang.Thread, Int]]" shouldNot compile
  }
}
