package pl.iterators.kebs_examples

import com.github.tminglei.slickpg.{ExPostgresDriver, PgArraySupport}

object ListValueCommonType {
  case class Institution(value: Long)
  case class MarketFinancialProduct(value: String)

  object BeforeKebs {
    object MyPostgresProfile extends ExPostgresDriver with PgArraySupport {
      override val api: API = new API {}
      trait API extends super.API with ArrayImplicits {
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
    object MyPostgresProfile extends ExPostgresDriver with PgArraySupport {
      override val api: API = new API {}
      trait API extends super.API with ArrayImplicits
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
    import pl.iterators.kebs.Kebs
    object MyPostgresProfile extends ExPostgresDriver with PgArraySupport {
      override val api: API = new API {}
      trait API extends super.API with ArrayImplicits with Kebs
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
