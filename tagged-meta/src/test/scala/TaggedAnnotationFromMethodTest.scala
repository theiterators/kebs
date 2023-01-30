import org.scalatest._
import pl.iterators.kebs.tagged._
import pl.iterators.kebs.tag.meta.tagged
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

@tagged object DomainTrimmedString {
  trait ProjectNameTag
  type Name = String @@ ProjectNameTag

  object Name {
    sealed trait Error
    case object Empty extends Error

    def validate(name: String) = if (name.trim.isEmpty) Left(Empty) else Right(name.trim)
  }
}

class TaggedAnnotationFromMethodTest extends AnyFunSuite with Matchers with EitherValues {
  import DomainTrimmedString._

  test("from method must use result from validation") {
    Name("   name   ") shouldEqual "name"
  }

  test("from method must forward Error value") {
    val result = try {
      Name("       ")
    } catch {
      case _: IllegalArgumentException => 1
      case _: Throwable                => 0
    }
    result shouldEqual 1
  }
}
