
import slick.jdbc.PostgresProfile.api._
import pl.iterators.kebs.tagged._
import pl.iterators.kebs.tagged.slick.{SlickSupport}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class SlickTaggedColumnTypeTests extends AnyFunSuite with Matchers with SlickSupport {
  trait IdTag
  type Id = Long @@ IdTag

  trait NameTag
  type Name = String @@ NameTag

  case class Row(id: Id, name: Name, num: Long)

  test("MappedColumnType for tagged types") {
    "implicitly[BaseColumnType[Long @@ IdTag]]" should compile
  }

  test("MappedColumnType for tagged types (alias)") {
    "implicitly[BaseColumnType[Id]]" should compile
  }
}
