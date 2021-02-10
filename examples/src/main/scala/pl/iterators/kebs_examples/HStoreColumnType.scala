package pl.iterators.kebs_examples

import com.github.tminglei.slickpg.{ExPostgresProfile, PgHStoreSupport}
import org.postgresql.util.HStoreConverter
import slick.jdbc.JdbcType

object HStoreColumnType {
  case class LanguageName(name: String)
  case class LanguageImportance(importance: Int)
  case class CategoryName(name: String)
  case class CategoryImportance(importance: Int)

  object BeforeKebs {
    import scala.jdk.CollectionConverters._

    object MyPostgresProfile extends ExPostgresProfile with PgHStoreSupport {
      override val api: APIWithHstore = new APIWithHstore {}
      trait APIWithHstore extends super.API with HStoreImplicits {
        implicit val languageHStoreTypeMapper: JdbcType[Map[LanguageName, LanguageImportance]] =
          new GenericJdbcType[Map[LanguageName, LanguageImportance]](
            "hstore",
            (v) =>
              HStoreConverter
                .fromString(v)
                .asScala
                .map { case (key, value) => (LanguageName(key), LanguageImportance(value.toInt)) }
                .toMap,
            (v) => HStoreConverter.toString(v.map { case (key, value) => (key.name, value.importance.toString) }.asJava),
            hasLiteralForm = false
          )

        implicit val categoryHStoreTypeMapper: JdbcType[Map[CategoryName, CategoryImportance]] =
          new GenericJdbcType[Map[CategoryName, CategoryImportance]](
            "hstore",
            (v) =>
              HStoreConverter
                .fromString(v)
                .asScala
                .map { case (key, value) => (CategoryName(key), CategoryImportance(value.toInt)) }
                .toMap,
            (v) => HStoreConverter.toString(v.map { case (key, value) => (key.name, value.importance.toString) }.asJava),
            hasLiteralForm = false
          )
      }
    }

    import MyPostgresProfile.api._

    class HStoreTestTable(tag: Tag)
        extends Table[(Long, Map[LanguageName, LanguageImportance], Map[CategoryName, CategoryImportance])](tag, "HStoreTest") {
      def id         = column[Long]("id", O.AutoInc, O.PrimaryKey)
      def languages  = column[Map[LanguageName, LanguageImportance]]("languages")
      def categories = column[Map[CategoryName, CategoryImportance]]("categories")

      def * = (id, languages, categories)
    }
  }

  object AfterKebs {
    object MyPostgresProfile extends ExPostgresProfile with PgHStoreSupport {
      override val api: APIWithHstore = new APIWithHstore {}
      trait APIWithHstore extends super.API with HStoreImplicits {}
    }

    import MyPostgresProfile.api._
    import pl.iterators.kebs._

    class HStoreTestTable(tag: Tag)
        extends Table[(Long, Map[LanguageName, LanguageImportance], Map[CategoryName, CategoryImportance])](tag, "HStoreTest") {
      def id         = column[Long]("id", O.AutoInc, O.PrimaryKey)
      def languages  = column[Map[LanguageName, LanguageImportance]]("languages")
      def categories = column[Map[CategoryName, CategoryImportance]]("categories")

      def * = (id, languages, categories)
    }
  }
}
