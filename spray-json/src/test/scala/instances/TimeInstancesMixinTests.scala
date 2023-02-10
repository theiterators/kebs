package instances

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.instances.time.LocalDateTimeString
import pl.iterators.kebs.instances.time.mixins.{DurationNanosLong, InstantEpochMilliLong}
import pl.iterators.kebs.instances.{InstanceConverter, TimeInstances}
import pl.iterators.kebs.json.KebsSpray
import pl.iterators.kebs.macros.base.CaseClass1Rep
import spray.json._

import java.time._
import java.time.format.DateTimeFormatter

class TimeInstancesMixinTests extends AnyFunSuite with Matchers {

  test("Instant epoch milli format") {
    object TimeInstancesProtocol extends DefaultJsonProtocol with KebsSpray with InstantEpochMilliLong
    import TimeInstancesProtocol._

    "implicitly[CaseClass1Rep[Instant, Long]]" shouldNot typeCheck
    "implicitly[CaseClass1Rep[Long, Instant]]" shouldNot typeCheck

    val jf    = implicitly[JsonFormat[Instant]]
    val value = 123456789
    val obj   = Instant.ofEpochMilli(value)

    jf.write(obj) shouldBe JsNumber(value)
    jf.read(JsNumber(value)) shouldBe obj
  }

  test("Duration nanos format, Instant epoch milli format") {
    object TimeInstancesProtocol extends DefaultJsonProtocol with KebsSpray with DurationNanosLong with InstantEpochMilliLong
    import TimeInstancesProtocol._

    "implicitly[CaseClass1Rep[Instant, Long]]" shouldNot typeCheck
    "implicitly[CaseClass1Rep[Long, Instant]]" shouldNot typeCheck
    "implicitly[CaseClass1Rep[Duration, Long]]" shouldNot typeCheck
    "implicitly[CaseClass1Rep[Long, Duration]]" shouldNot typeCheck

    val jf_duration    = implicitly[JsonFormat[Duration]]
    val value_duration = 123456789
    val obj_duration   = Duration.ofNanos(value_duration)

    val jf_instant    = implicitly[JsonFormat[Instant]]
    val value_instant = 123456789
    val obj_instant   = Instant.ofEpochMilli(value_instant)

    jf_duration.write(obj_duration) shouldBe JsNumber(value_duration)
    jf_duration.read(JsNumber(value_duration)) shouldBe obj_duration

    jf_instant.write(obj_instant) shouldBe JsNumber(value_instant)
    jf_instant.read(JsNumber(value_instant)) shouldBe obj_instant
  }

  test("LocalDateTime custom format using companion object") {
    object TimeInstancesProtocol extends DefaultJsonProtocol with KebsSpray with LocalDateTimeString {
      val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")

      override implicit val localDateTimeFormatter: InstanceConverter[LocalDateTime, String] =
        InstanceConverter.apply[LocalDateTime, String](_.format(formatter), LocalDateTime.parse(_, formatter))
    }
    import TimeInstancesProtocol._

    "implicitly[CaseClass1Rep[LocalDateTime, String]]" shouldNot typeCheck
    "implicitly[CaseClass1Rep[String, LocalDateTime]]" shouldNot typeCheck

    val jf    = implicitly[JsonFormat[LocalDateTime]]
    val value = "2007/12/03 10:30"
    val obj   = LocalDateTime.parse(value, formatter)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("LocalDateTime custom format with error handling") {
    object TimeInstancesProtocol extends DefaultJsonProtocol with KebsSpray with TimeInstances {
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

    "implicitly[CaseClass1Rep[LocalDateTime, String]]" shouldNot typeCheck
    "implicitly[CaseClass1Rep[String, LocalDateTime]]" shouldNot typeCheck

    val jf    = implicitly[JsonFormat[LocalDateTime]]
    val value = "2007/12/03 10:30"
    val obj   = LocalDateTime.parse(value, formatter)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }
}
