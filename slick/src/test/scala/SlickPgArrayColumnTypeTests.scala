import com.github.tminglei.slickpg._
import enumeratum.{Enum, EnumEntry}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class SlickPgArrayColumnTypeTests extends AnyFunSuite with Matchers {
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

  sealed trait AnEnum extends EnumEntry
  object AnEnum extends Enum[AnEnum] {
    case object Soomething    extends AnEnum
    case object SomethingElse extends AnEnum

    override val values = findValues
  }
  import enums._

  test("seqValueColumnType with enums") {
    """
      |    class EnumSeqTestTable(tag: Tag) extends Table[(Long, Seq[AnEnum])](tag, "EnumSeqTest") {
      |      def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |      def enums = column[Seq[AnEnum]]("enums")
      |
      |      def * = (id, enums)
      |    }
    """.stripMargin should compile
  }

  test("seqValueColumnType with enums and not enums") {
    """
      |    class EnumSeqTestTable(tag: Tag) extends Table[(Long, Seq[Institution], Seq[AnEnum])](tag, "EnumSeqTest") {
      |      def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |      def institutions = column[Seq[Institution]]("institutions")
      |      def enums = column[Seq[AnEnum]]("enums")
      |
      |      def * = (id, institutions, enums)
      |    }
      """.stripMargin should compile
  }
}
