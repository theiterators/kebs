package pl.iterators.kebs.slick.arrays

import com.github.tminglei.slickpg._
import enumeratum.{Enum, EnumEntry}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.enumeratum.KebsEnumeratum

class SlickPgArrayColumnTypeTests extends AnyFunSuite with Matchers with KebsEnumeratum {
  case class Institution(value: Long)
  case class MarketFinancialProduct(value: String)

  import pl.iterators.kebs.slick.KebsSlickSupport

  object MyPostgresProfile extends ExPostgresProfile with PgArraySupport with KebsSlickSupport {
    override val api: APIWithArrays = new APIWithArrays {}
    trait APIWithArrays
        extends ExtPostgresAPI
        with ArrayImplicits
        with KebsBasicImplicits
        with KebsValueClassLikeImplicits
        with CaseClass1ToValueClass
        with KebsEnumImplicits
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

  sealed trait AnEnum extends EnumEntry
  object AnEnum extends Enum[AnEnum] {
    case object Something     extends AnEnum
    case object SomethingElse extends AnEnum

    override val values = findValues
  }

  test("List column type with enums") {
    """
      |    class EnumSeqTestTable(tag: Tag) extends Table[(Long, List[AnEnum])](tag, "EnumSeqTest") {
      |      def id    = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |      def enums = column[List[AnEnum]]("enums")
      |
      |      def * = (id, enums)
      |    }
    """.stripMargin should compile
  }

  test("List column type with enums and not enums") {
    """
      |    class EnumSeqTestTable(tag: Tag) extends Table[(Long, List[Institution], List[AnEnum])](tag, "EnumSeqTest") {
      |      def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |      def institutions = column[List[Institution]]("institutions")
      |      def enums = column[List[AnEnum]]("enums")
      |
      |      def * = (id, institutions, enums)
      |    }
      """.stripMargin should compile
  }
}
