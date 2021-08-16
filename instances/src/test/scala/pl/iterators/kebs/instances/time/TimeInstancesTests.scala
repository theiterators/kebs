package pl.iterators.kebs.instances.time

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pl.iterators.kebs.instances.InstanceConverter

import java.time.Duration

class TimeInstancesTests extends AnyFunSuite with DurationString {
  test("Duration to String") {
    val ico   = implicitly[InstanceConverter[Duration, String]]
    val value = "PT1H"
    val obj   = Duration.parse(value)

    ico.decode(value) shouldEqual obj
    ico.encode(obj) shouldEqual value
  }
}
