package pl.iterators.kebs.examples

import com.github.tminglei.slickpg._

object ListValueCommonType {
  case class Institution(value: Long)
  case class MarketFinancialProduct(value: String)

  object BeforeKebs {
    object MyPostgresProfile extends ExPostgresProfile with PgArraySupport {
      override val api: APIWithArrays = new APIWithArrays {}
      trait APIWithArrays extends super.API with ArrayImplicits {
        implicit val institutionListTypeWrapper =
          new SimpleArrayJdbcType[Long]("int8").mapTo[Institution](Institution, _.value).to(_.toList)
        implicit val marketFinancialProductWrapper =
          new SimpleArrayJdbcType[String]("text").mapTo[MarketFinancialProduct](MarketFinancialProduct, _.value).to(_.toList)
      }
    }

    import MyPostgresProfile.api._

    class ArrayTestTable(tag: Tag) extends Table[(Long, List[Institution], Option[List[MarketFinancialProduct]])](tag, "ArrayTest") {
      def id                   = column[Long]("id", O.AutoInc, O.PrimaryKey)
      def institutions         = column[List[Institution]]("institutions")
      def mktFinancialProducts = column[Option[List[MarketFinancialProduct]]]("mktFinancialProducts")

      def * = (id, institutions, mktFinancialProducts)
    }
  }

  object AfterKebs {
    object MyPostgresProfile extends ExPostgresProfile with PgArraySupport {
      override val api: APIWithArrays = new APIWithArrays {}
      trait APIWithArrays extends super.API with ArrayImplicits
    }

    import MyPostgresProfile.api._
    import pl.iterators.kebs._
    class ArrayTestTable(tag: Tag) extends Table[(Long, List[Institution], Option[List[MarketFinancialProduct]])](tag, "ArrayTest") {
      def id                   = column[Long]("id", O.AutoInc, O.PrimaryKey)
      def institutions         = column[List[Institution]]("institutions")
      def mktFinancialProducts = column[Option[List[MarketFinancialProduct]]]("mktFinancialProducts")

      def * = (id, institutions, mktFinancialProducts)
    }
  }

  object AfterKebsTraitStyle {

    import pl.iterators.kebs.slick.BasicSlickSupport
    object MyPostgresProfile extends ExPostgresProfile with PgArraySupport {
      override val api: APIWithArraysAndKebs = new APIWithArraysAndKebs {}
      trait APIWithArraysAndKebs extends super.API with ArrayImplicits with BasicSlickSupport
    }

    import MyPostgresProfile.api._
    class ArrayTestTable(tag: Tag) extends Table[(Long, List[Institution], Option[List[MarketFinancialProduct]])](tag, "ArrayTest") {
      def id                   = column[Long]("id", O.AutoInc, O.PrimaryKey)
      def institutions         = column[List[Institution]]("institutions")
      def mktFinancialProducts = column[Option[List[MarketFinancialProduct]]]("mktFinancialProducts")

      def * = (id, institutions, mktFinancialProducts)
    }
  }
}
