package pl.iterators.kebs.instances

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.json.KebsSpray
import spray.json._

import java.time.{DayOfWeek, Duration, Instant}

class TimeInstancesTests extends AnyFunSuite with Matchers {

  test("DayOfWeek Int format") {
    object TimeInstancesProtocol extends DefaultJsonProtocol with KebsSpray with TimeInstances
    import TimeInstancesProtocol._

    val jf    = implicitly[JsonFormat[DayOfWeek]]
    val value = 1
    val obj   = DayOfWeek.of(value)
    jf.write(obj) shouldBe JsNumber(value)
    jf.read(JsNumber(value)) shouldBe obj
  }

  test("DayOfWeek wrong format exception") {
    object TimeInstancesProtocol extends DefaultJsonProtocol with KebsSpray with TimeInstances
    import TimeInstances.{DayOfWeekFormat, FormatMsg}
    import TimeInstancesProtocol._

    val jf    = implicitly[JsonFormat[DayOfWeek]]
    val value = 8

    val thrown = intercept[IllegalArgumentException] {
      jf.read(JsNumber(value))
    }
    assert(thrown.getMessage === FormatMsg[DayOfWeek, Int](classOf[DayOfWeek], value, DayOfWeekFormat))
  }

  test("Duration String format") {
    object TimeInstancesProtocol extends DefaultJsonProtocol with KebsSpray with TimeInstances
    import TimeInstancesProtocol._

    val jf    = implicitly[JsonFormat[Duration]]
    val value = "PT1H"
    val obj   = Duration.parse(value)
    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("Duration wrong format exception") {
    object TimeInstancesProtocol extends DefaultJsonProtocol with KebsSpray with TimeInstances
    import TimeInstances.{DurationFormat, FormatMsg}
    import TimeInstancesProtocol._

    val jf    = implicitly[JsonFormat[Duration]]
    val value = "ThisIsNotADuration"

    val thrown = intercept[IllegalArgumentException] {
      jf.read(JsString(value))
    }
    assert(thrown.getMessage === FormatMsg[Duration, String](classOf[Duration], value, DurationFormat))
  }

  test("Duration nanos format") {
    import TimeInstances.DurationNanos
    object TimeInstancesProtocol extends DefaultJsonProtocol with KebsSpray with TimeInstances with DurationNanos
    import TimeInstancesProtocol._

    val jf    = implicitly[JsonFormat[Duration]]
    val value = 123456789
    val obj   = Duration.ofNanos(value)
    jf.write(obj) shouldBe JsNumber(value)
    jf.read(JsNumber(value)) shouldBe obj
  }

  test("Instant String format") {
    object TimeInstancesProtocol extends DefaultJsonProtocol with KebsSpray with TimeInstances
    import TimeInstancesProtocol._

    val jf    = implicitly[JsonFormat[Instant]]
    val value = "2007-12-03T10:15:30Z"

    val obj = Instant.parse(value)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("Instant wrong format exception") {
    object TimeInstancesProtocol extends DefaultJsonProtocol with KebsSpray with TimeInstances
    import TimeInstances.{FormatMsg, InstantFormat}
    import TimeInstancesProtocol._

    val jf    = implicitly[JsonFormat[Instant]]
    val value = "ThisIsNotAnInstant"

    val thrown = intercept[IllegalArgumentException] {
      jf.read(JsString(value))
    }
    assert(thrown.getMessage === FormatMsg[Instant, String](classOf[Instant], value, InstantFormat))
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
    jf_duration.write(obj_duration) shouldBe JsNumber(value_duration)
    jf_duration.read(JsNumber(value_duration)) shouldBe obj_duration

    val jf_instant    = implicitly[JsonFormat[Instant]]
    val value_instant = 123456789
    val obj_instant   = Instant.ofEpochMilli(value_instant)
    jf_instant.write(obj_instant) shouldBe JsNumber(value_instant)
    jf_instant.read(JsNumber(value_instant)) shouldBe obj_instant
  }

}
