package pl.iterators.kebs.circe.instances

import io.circe.{Decoder, Encoder, Json}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.circe.KebsCirce

import java.time._
import java.time.format.DateTimeFormatter

import pl.iterators.kebs.core.instances.InstanceConverter
import pl.iterators.kebs.instances.TimeInstances
import pl.iterators.kebs.instances.time.LocalDateTimeString
import pl.iterators.kebs.instances.time.mixins.{InstantEpochMilliLong, DurationNanosLong}

class TimeInstancesMixinTests extends AnyFunSuite with Matchers {

  test("Instant epoch milli format") {
    object TimeInstancesProtocol extends KebsCirce with InstantEpochMilliLong
    import TimeInstancesProtocol.{given, _}

    "implicitly[ValueClassLike[Instant, Long]]" shouldNot typeCheck
    "implicitly[ValueClassLike[Long, Instant]]" shouldNot typeCheck

    val decoder = implicitly[Decoder[Instant]]
    val encoder = implicitly[Encoder[Instant]]
    val value   = 123456789
    val obj     = Instant.ofEpochMilli(value)

    encoder(obj) shouldBe Json.fromInt(value)
    decoder(Json.fromInt(value).hcursor) shouldBe Right(obj)
  }

  test("Duration nanos format, Instant epoch milli format") {
    object TimeInstancesProtocol extends KebsCirce with DurationNanosLong with InstantEpochMilliLong
    import TimeInstancesProtocol.{given, _}

    "implicitly[ValueClassLike[Instant, Long]]" shouldNot typeCheck
    "implicitly[ValueClassLike[Long, Instant]]" shouldNot typeCheck
    "implicitly[ValueClassLike[Duration, Long]]" shouldNot typeCheck
    "implicitly[ValueClassLike[Long, Duration]]" shouldNot typeCheck

    val decoder_duration = implicitly[Decoder[Duration]]
    val encoder_duration = implicitly[Encoder[Duration]]
    val value_duration   = 123456789
    val obj_duration     = Duration.ofNanos(value_duration)

    val decoder_instant = implicitly[Decoder[Instant]]
    val encoder_instant = implicitly[Encoder[Instant]]
    val value_instant   = 123456789
    val obj_instant     = Instant.ofEpochMilli(value_instant)

    encoder_duration(obj_duration) shouldBe Json.fromInt(value_duration)
    decoder_duration(Json.fromInt(value_duration).hcursor) shouldBe Right(obj_duration)

    encoder_instant(obj_instant) shouldBe Json.fromInt(value_instant)
    decoder_instant(Json.fromInt(value_instant).hcursor) shouldBe Right(obj_instant)
  }

  test("LocalDateTime custom format using companion object") {
    object TimeInstancesProtocol extends KebsCirce with LocalDateTimeString {
      val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")

      override implicit val localDateTimeFormatter: InstanceConverter[LocalDateTime, String] =
        InstanceConverter.apply[LocalDateTime, String](_.format(formatter), LocalDateTime.parse(_, formatter))
    }
    import TimeInstancesProtocol.{given, _}

    "implicitly[ValueClassLike[LocalDateTime, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, LocalDateTime]]" shouldNot typeCheck

    val encoder = implicitly[Encoder[LocalDateTime]]
    val decoder = implicitly[Decoder[LocalDateTime]]
    val value   = "2007/12/03 10:30"
    val obj     = LocalDateTime.parse(value, formatter)

    encoder(obj) shouldBe Json.fromString(value)
    decoder(Json.fromString(value).hcursor) shouldBe Right(obj)
  }

  test("LocalDateTime custom format with error handling") {
    object TimeInstancesProtocol extends KebsCirce with TimeInstances {
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
    import TimeInstancesProtocol.{given, _}

    "implicitly[ValueClassLike[LocalDateTime, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, LocalDateTime]]" shouldNot typeCheck

    val encoder = implicitly[Encoder[LocalDateTime]]
    val decoder = implicitly[Decoder[LocalDateTime]]
    val value   = "2007/12/03 10:30"
    val obj     = LocalDateTime.parse(value, formatter)

    encoder(obj) shouldBe Json.fromString(value)
    decoder(Json.fromString(value).hcursor) shouldBe Right(obj)
  }
}
