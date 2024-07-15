package pl.iterators.kebs.json.instances

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.instances.TimeInstances
import pl.iterators.kebs.instances.time.LocalDateTimeString
import pl.iterators.kebs.instances.time.mixins.{DurationNanosLong, InstantEpochMilliLong}
import pl.iterators.kebs.core.instances.InstanceConverter
import pl.iterators.kebs.json.KebsPlay
import play.api.libs.json.{Format, JsNumber, JsString, JsSuccess}

import java.time.*
import java.time.format.DateTimeFormatter

class TimeInstancesMixinTests extends AnyFunSuite with Matchers with KebsPlay {
  test("Instant epoch milli format") {
    object TimeInstancesProtocol extends  InstantEpochMilliLong
    import TimeInstancesProtocol._

    "implicitly[ValueClassLike[Instant, Long]]" shouldNot typeCheck
    "implicitly[ValueClassLike[Long, Instant]]" shouldNot typeCheck

    val jf    = implicitly[Format[Instant]]
    val value = 123456789
    val obj   = Instant.ofEpochMilli(value)

    jf.writes(obj) shouldBe JsNumber(value)
    jf.reads(JsNumber(value)) shouldBe JsSuccess(obj)
  }

  test("Duration nanos format, Instant epoch milli format") {
    object TimeInstancesProtocol extends DurationNanosLong with InstantEpochMilliLong
    import TimeInstancesProtocol._

    "implicitly[ValueClassLike[Instant, Long]]" shouldNot typeCheck
    "implicitly[ValueClassLike[Long, Instant]]" shouldNot typeCheck
    "implicitly[ValueClassLike[Duration, Long]]" shouldNot typeCheck
    "implicitly[ValueClassLike[Long, Duration]]" shouldNot typeCheck

    val jf_duration    = implicitly[Format[Duration]]
    val value_duration = 123456789
    val obj_duration   = Duration.ofNanos(value_duration)

    val jf_instant    = implicitly[Format[Instant]]
    val value_instant = 123456789
    val obj_instant   = Instant.ofEpochMilli(value_instant)

    jf_duration.writes(obj_duration) shouldBe JsNumber(value_duration)
    jf_duration.reads(JsNumber(value_duration)) shouldBe JsSuccess(obj_duration)

    jf_instant.writes(obj_instant) shouldBe JsNumber(value_instant)
    jf_instant.reads(JsNumber(value_instant)) shouldBe JsSuccess(obj_instant)
  }

  test("LocalDateTime custom format using companion object") {
    object TimeInstancesProtocol extends  LocalDateTimeString {
      val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")

      override implicit val localDateTimeFormatter: InstanceConverter[LocalDateTime, String] =
        InstanceConverter.apply[LocalDateTime, String](_.format(formatter), LocalDateTime.parse(_, formatter))
    }
    import TimeInstancesProtocol._

    "implicitly[ValueClassLike[LocalDateTime, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, LocalDateTime]]" shouldNot typeCheck

    val jf    = implicitly[Format[LocalDateTime]]
    val value = "2007/12/03 10:30"
    val obj   = LocalDateTime.parse(value, formatter)

    jf.writes(obj) shouldBe JsString(value)
    jf.reads(JsString(value)) shouldBe JsSuccess(obj)
  }

  test("LocalDateTime custom format with error handling") {
    object TimeInstancesProtocol extends TimeInstances {
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
                  e)
              case e: Throwable => throw e
            }
        }
    }
    import TimeInstancesProtocol._

    "implicitly[ValueClassLike[LocalDateTime, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, LocalDateTime]]" shouldNot typeCheck

    val jf    = implicitly[Format[LocalDateTime]]
    val value = "2007/12/03 10:30"
    val obj   = LocalDateTime.parse(value, formatter)

    jf.writes(obj) shouldBe JsString(value)
    jf.reads(JsString(value)) shouldBe JsSuccess(obj)
  }
}
