package pl.iterators.kebs.jsoniter.instances

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.jsoniter.KebsJsoniter
import com.github.plokhotnyuk.jsoniter_scala.core._
import pl.iterators.kebs.core.instances.InstanceConverter
import pl.iterators.kebs.instances.TimeInstances
import pl.iterators.kebs.instances.time.LocalDateTimeString
import pl.iterators.kebs.instances.time.mixins.{DurationNanosLong, InstantEpochMilliLong}

import java.time._
import java.time.format.DateTimeFormatter

class TimeInstancesMixinTests extends AnyFunSuite with Matchers {

  test("Instant epoch milli format") {
    object TimeInstancesProtocol extends KebsJsoniter with InstantEpochMilliLong
    import TimeInstancesProtocol._

    "implicitly[ValueClassLike[Instant, Long]]" shouldNot typeCheck
    "implicitly[ValueClassLike[Long, Instant]]" shouldNot typeCheck

    val codec = implicitly[JsonValueCodec[Instant]]
    val value = 123456789
    val obj   = Instant.ofEpochMilli(value)

    writeToString[Instant](obj)(codec) shouldBe value.toString
    readFromString[Instant](value.toString)(codec) shouldBe obj
  }

  test("Duration nanos format, Instant epoch milli format") {
    object TimeInstancesProtocol extends KebsJsoniter with DurationNanosLong with InstantEpochMilliLong
    import TimeInstancesProtocol._

    "implicitly[ValueClassLike[Instant, Long]]" shouldNot typeCheck
    "implicitly[ValueClassLike[Long, Instant]]" shouldNot typeCheck
    "implicitly[ValueClassLike[Duration, Long]]" shouldNot typeCheck
    "implicitly[ValueClassLike[Long, Duration]]" shouldNot typeCheck

    val codec_duration = implicitly[JsonValueCodec[Duration]]
    val value_duration = 123456789
    val obj_duration   = Duration.ofNanos(value_duration)

    val codec_instant = implicitly[JsonValueCodec[Instant]]
    val value_instant = 123456789
    val obj_instant   = Instant.ofEpochMilli(value_instant)

    writeToString[Duration](obj_duration)(codec_duration) shouldBe value_duration.toString
    readFromString[Duration](value_duration.toString)(codec_duration) shouldBe obj_duration

    writeToString[Instant](obj_instant)(codec_instant) shouldBe value_instant.toString
    readFromString[Instant](value_instant.toString)(codec_instant) shouldBe obj_instant
  }

  test("LocalDateTime custom format using companion object") {
    object TimeInstancesProtocol extends KebsJsoniter with LocalDateTimeString {
      val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")

      override implicit val localDateTimeFormatter: InstanceConverter[LocalDateTime, String] =
        InstanceConverter.apply[LocalDateTime, String](_.format(formatter), LocalDateTime.parse(_, formatter))
    }
    import TimeInstancesProtocol._

    "implicitly[ValueClassLike[LocalDateTime, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, LocalDateTime]]" shouldNot typeCheck

    val codec = implicitly[JsonValueCodec[LocalDateTime]]
    val value = "2007/12/03 10:30"
    val obj   = LocalDateTime.parse(value, formatter)

    writeToString[LocalDateTime](obj)(codec) shouldBe "\"" + value + "\""
    readFromString[LocalDateTime]("\"" + value + "\"")(codec) shouldBe obj
  }

  test("LocalDateTime custom format with error handling") {
    object TimeInstancesProtocol extends KebsJsoniter with TimeInstances {
      val pattern                      = "yyyy/MM/dd HH:mm"
      val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(pattern)

      override implicit val localDateTimeFormatter: InstanceConverter[LocalDateTime, String] =
        new InstanceConverter[LocalDateTime, String] {
          override def encode(obj: LocalDateTime): String = obj.format(formatter)
          override def decode(value: String): LocalDateTime =
            try {
              LocalDateTime.parse(value, formatter)
            } catch {
              case e: DateTimeException =>
                throw new IllegalArgumentException(
                  s"${classOf[LocalDateTime]} cannot be parsed from $value – should be in format $pattern",
                  e
                )
              case e: Throwable => throw e
            }
        }
    }
    import TimeInstancesProtocol._

    "implicitly[ValueClassLike[LocalDateTime, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, LocalDateTime]]" shouldNot typeCheck

    val codec = implicitly[JsonValueCodec[LocalDateTime]]
    val value = "2007/12/03 10:30"
    val obj   = LocalDateTime.parse(value, formatter)

    writeToString[LocalDateTime](obj)(codec) shouldBe "\"" + value + "\""
    readFromString[LocalDateTime]("\"" + value + "\"")(codec) shouldBe obj
  }
}
