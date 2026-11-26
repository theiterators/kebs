package pl.iterators.kebs.sprayjson.formats

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.sprayjson.KebsSprayJson
import spray.json._

object SprayAmbiguityTestDomain {
  final case class AmbT(a: Int, b: Int)
  final case class HasAmb(x: AmbT)
}

// Scala 3 only: scalac reports the ambiguity on 2.13 itself, as a hard error typeCheck cannot capture
class SprayAmbiguityTests extends AnyFunSuite with Matchers {
  import SprayAmbiguityTestDomain._

  object Ambiguous extends DefaultJsonProtocol with KebsSprayJson {
    implicit val first: JsonFormat[AmbT]  = jsonFormat2(AmbT.apply)
    implicit val second: JsonFormat[AmbT] = jsonFormat2(AmbT.apply)
  }

  test("ambiguous field formats are an error, not a reason to derive") {
    import Ambiguous._
    "implicitly[RootJsonFormat[HasAmb]]" shouldNot typeCheck
  }
}
