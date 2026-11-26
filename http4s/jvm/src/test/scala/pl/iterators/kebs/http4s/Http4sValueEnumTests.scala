package pl.iterators.kebs.http4s

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import enumeratum.values._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.enums.ValueEnumLikeEntry
import pl.iterators.kebs.enumeratum.KebsValueEnumeratum
import pl.iterators.kebs.testkit.model.Level

object Http4sValueEnumTestDomain {
  sealed abstract class Code(val value: String) extends StringEnumEntry with ValueEnumLikeEntry[String]
  object Code                                   extends StringEnum[Code] {
    case object Good extends Code("g")
    case object Bad  extends Code("b")
    val values = findValues
  }
  sealed abstract class Big(val value: Long) extends LongEnumEntry with ValueEnumLikeEntry[Long]
  object Big                                 extends LongEnum[Big] {
    case object Max extends Big(9999999999L)
    val values = findValues
  }
  sealed abstract class Small(val value: Short) extends ShortEnumEntry with ValueEnumLikeEntry[Short]
  object Small                                  extends ShortEnum[Small] {
    case object One extends Small(1)
    val values = findValues
  }
  sealed abstract class Tiny(val value: Byte) extends ByteEnumEntry with ValueEnumLikeEntry[Byte]
  object Tiny                                 extends ByteEnum[Tiny] {
    case object One extends Tiny(1)
    val values = findValues
  }
}

class Http4sValueEnumTests extends AnyFunSuite with Matchers with KebsValueEnumeratum {
  import Http4sValueEnumTestDomain._

  implicit private val runtime: IORuntime = IORuntime.global

  private object LevelParam extends QueryParamDecoderMatcher[Level]("level")

  private val LevelVar = ValueEnumInt[Level]

  private val routes = HttpRoutes.of[IO] {
    case GET -> Root / "path" / LevelVar(level)     => Ok(level.toString)
    case GET -> Root / "query" :? LevelParam(level) => Ok(level.toString)
  }

  private def body(path: Uri): String =
    routes.orNotFound.run(Request(method = Method.GET, uri = path)).flatMap(_.as[String]).unsafeRunSync()

  test("path variables") {
    ValueEnumInt[Level].unapply("2") shouldBe Some(Level.High)
    ValueEnumString[Code].unapply("g") shouldBe Some(Code.Good)
    ValueEnumLong[Big].unapply("9999999999") shouldBe Some(Big.Max)
    ValueEnumShort[Small].unapply("1") shouldBe Some(Small.One)
    ValueEnumByte[Tiny].unapply("1") shouldBe Some(Tiny.One)
  }

  test("path variables that are not a value of the enum") {
    List("9", "x", "", "2.0", "99999999999").foreach(ValueEnumInt[Level].unapply(_) shouldBe None)
    List("G", "good", "").foreach(ValueEnumString[Code].unapply(_) shouldBe None)
    List("2", "40000").foreach(ValueEnumShort[Small].unapply(_) shouldBe None)
  }

  test("in a route's path") {
    body(uri"/path/2") shouldBe "High"
    body(uri"/path/9") shouldBe "Not found"
    body(uri"/path/x") shouldBe "Not found"
  }

  test("in a route's query") {
    body(uri"/query?level=1") shouldBe "Low"
    body(uri"/query?level=9") shouldBe "Not found"
  }

  test("query parameters") {
    def decoded[T: QueryParamDecoder](value: String) =
      QueryParamDecoder[T].decode(QueryParameterValue(value)).toEither.left.map(_.head.sanitized)
    decoded[Level]("2") shouldBe Right(Level.High)
    decoded[Code]("b") shouldBe Right(Code.Bad)
    decoded[Big]("9999999999") shouldBe Right(Big.Max)
    decoded[Small]("1") shouldBe Right(Small.One)
    decoded[Level]("9") shouldBe Left("9 is not a member of 1, 2")
    decoded[Level]("x").isLeft shouldBe true
  }
}
