import slick.jdbc.PostgresProfile
import pl.iterators.kebs.tagged._
import pl.iterators.kebs.tagged.slick.KebsTaggedSlickSupport
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class SlickTaggedColumnTypeTests extends AnyFunSuite with Matchers {

  object MyPostgresProfile extends PostgresProfile with KebsTaggedSlickSupport {
    override val api: APITagged = new APITagged {}
    trait APITagged extends JdbcAPI with KebsTaggedImplicits
  }

  import MyPostgresProfile.api._

  trait IdTag
  type Id = Long @@ IdTag

  trait NameTag
  type Name = String @@ NameTag

  test("MappedColumnType for tagged types") {
    "implicitly[BaseColumnType[Long @@ IdTag]]" should compile
  }

  test("MappedColumnType for tagged types (alias)") {
    "implicitly[BaseColumnType[Id]]" should compile
  }
}
