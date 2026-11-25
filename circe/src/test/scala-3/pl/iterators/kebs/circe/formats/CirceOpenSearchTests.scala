package pl.iterators.kebs.circe.formats

import cats.data.NonEmptyList
import io.circe.{Decoder, Encoder}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.circe.KebsCirce
import pl.iterators.kebs.testkit.nested.*

object CirceOpenSearchTests {
  object Protocol extends KebsCirce
  import Protocol.*, CirceNestedInstancesTestDomain.PageCodecs.*

  final case class RecNel(v: Int, k: Option[NonEmptyList[RecNel]])
  final case class HasRec(nel: RecNel, rec: RecPage, adt: Adt) derives Encoder.AsObject, Decoder
}

// derivation that the compiler reaches while another macro's search is open
class CirceOpenSearchTests extends AnyFunSuite with Matchers {
  import CirceOpenSearchTests.*

  test("inside a derives clause") {
    val value = HasRec(RecNel(1, Some(NonEmptyList.of(RecNel(2, None)))), recPage, adt)
    val json  = """{"nel":{"v":1,"k":[{"v":2,"k":null}]},"rec":{"v":1,"kids":{"DATA":[{"v":2,"kids":{"DATA":[]}}]}},""" +
      """"adt":{"Branch":{"l":{"DATA":[{"Leaf":{"n":1,"i":{"name":"i","n":1}}},{"Empty":{}}]},"r":[{"Empty":{}}]}}}"""
    Encoder[HasRec].apply(value).noSpaces shouldBe json
    io.circe.parser.decode[HasRec](json) shouldBe Right(value)
  }
}
