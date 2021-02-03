package pl.iterators.kebs.instances

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.instances.TimeInstances.{DecodeError, Formatter}
import pl.iterators.kebs.json.KebsSpray
import spray.json._

import java.time._
import java.time.format.DateTimeFormatter

class TimeInstancesMixinTests extends AnyFunSuite with Matchers {

  test("LocalDateTime custom format") {
    object TimeInstancesProtocol extends DefaultJsonProtocol with KebsSpray with TimeInstances {
      implicit val localDateTimeFormatter: Formatter[LocalDateTime, String] = new Formatter[LocalDateTime, String] {
        override def encode(obj: LocalDateTime): String                        = obj.format(formatter)
        override def decode(value: String): Either[DecodeError, LocalDateTime] = Right(LocalDateTime.parse(value, formatter))
      }
      val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
    }
    import TimeInstancesProtocol._

    val jf    = implicitly[JsonFormat[LocalDateTime]]
    val value = "2007/12/03 10:30"
    val obj   = LocalDateTime.parse(value, formatter)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("Instant epoch milli format") {
    import TimeInstances.InstantEpochMilli
    object TimeInstancesProtocol extends DefaultJsonProtocol with KebsSpray with TimeInstances with InstantEpochMilli
    import TimeInstancesProtocol._

    val jf    = implicitly[JsonFormat[Instant]]
    val value = 123456789
    val obj   = Instant.ofEpochMilli(value)

    jf.write(obj) shouldBe JsNumber(value)
    jf.read(JsNumber(value)) shouldBe obj
  }

  test("Duration nanos format, Instant epoch milli format") {
    import TimeInstances.{DurationNanos, InstantEpochMilli}
    object TimeInstancesProtocol extends DefaultJsonProtocol with KebsSpray with TimeInstances with DurationNanos with InstantEpochMilli
    import TimeInstancesProtocol._

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

}
