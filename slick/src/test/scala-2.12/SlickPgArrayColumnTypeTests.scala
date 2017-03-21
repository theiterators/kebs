import com.github.tminglei.slickpg._
import org.scalatest.{FunSuite, Matchers}

class SlickPgArrayColumnTypeTests extends FunSuite with Matchers {
  case class Institution(value: Long)
  case class MarketFinancialProduct(value: String)

  object MyPostgresProfile extends ExPostgresProfile with PgArraySupport {
    override val api: API = new API {}
    trait API extends super.API with ArrayImplicits
  }

  import MyPostgresProfile.api._
  import pl.iterators.kebs._
  test("Array mapping") {
    """
      |    class ArrayTestTable(tag: Tag) extends Table[(Long, List[Institution], Option[List[MarketFinancialProduct]])](tag, "ArrayTest") {
      |      def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |      def institutions = column[List[Institution]]("institutions")
      |      def mktFinancialProducts = column[Option[List[MarketFinancialProduct]]]("mktFinancialProducts")
      |
      |      def * = (id, institutions, mktFinancialProducts)
      |    }
    """.stripMargin should compile
  }

  test("seqValueColumnType") {
    """
      |    class SeqTestTable(tag: Tag) extends Table[(Long, Seq[Institution], Option[List[MarketFinancialProduct]])](tag, "SeqTest") {
      |      def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |      def institutions = column[Seq[Institution]]("institutions")
      |      def mktFinancialProducts = column[Option[List[MarketFinancialProduct]]]("mktFinancialProducts")
      |
      |      def * = (id, institutions, mktFinancialProducts)
      |    }
    """.stripMargin should compile
  }
}
