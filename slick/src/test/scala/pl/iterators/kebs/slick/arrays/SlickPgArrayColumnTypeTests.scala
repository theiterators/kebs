package pl.iterators.kebs.slick.arrays

import com.github.tminglei.slickpg._
import enumeratum.{Enum, EnumEntry}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class SlickPgArrayColumnTypeTests extends AnyFunSuite with Matchers {
  import pl.iterators.kebs.core.macros.CaseClass1ToValueClass._

  case class Institution(value: Long)
  case class MarketFinancialProduct(value: String)

  import pl.iterators.kebs.slick.Kebs
  import pl.iterators.kebs.slick.enums.KebsEnums

  object MyPostgresProfile extends ExPostgresProfile with PgArraySupport {
    override val api: ExtPostgresAPI = new ExtPostgresAPI {}
    trait APIWithArrays extends ExPostgresProfile with ArrayImplicits with Kebs with KebsEnums
  }

  import MyPostgresProfile.api._
  test("List column type") {
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

  test("Seq column type") {
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
    case object Something     extends AnEnum
    case object SomethingElse extends AnEnum

    override val values = findValues
  }
  import pl.iterators.kebs.slick.enums._
  import pl.iterators.kebs.enumeratum._

  test("Seq column type with enums") {
    """
      |    class EnumSeqTestTable(tag: Tag) extends Table[(Long, Seq[AnEnum])](tag, "EnumSeqTest") {
      |      def id    = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |      def enums = column[Seq[AnEnum]]("enums")
      |
      |      def * = (id, enums)
      |    }
    """.stripMargin should compile
  }

  test("Seq column type with enums and not enums") {
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
