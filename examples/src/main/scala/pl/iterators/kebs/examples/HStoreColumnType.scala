package pl.iterators.kebs.examples

import com.github.tminglei.slickpg.{ExPostgresProfile, PgHStoreSupport}
import org.postgresql.util.HStoreConverter
import slick.jdbc.JdbcType
import slick.lifted.MappedProjection

import java.time.YearMonth

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

        val yearMonthIso: Isomorphism[YearMonth, String] = new Isomorphism(_.toString, YearMonth.parse)
      }
    }

    import MyPostgresProfile.api._

    class HStoreTestTable(tag: Tag)
        extends Table[(
            Long,
            Map[LanguageName, LanguageImportance],
            Map[CategoryName, CategoryImportance],
            Map[YearMonth, Boolean]
        )](tag, "HStoreTest") {
      def id                                = column[Long]("id", O.AutoInc, O.PrimaryKey)
      def languages                         = column[Map[LanguageName, LanguageImportance]]("languages")
      def categories                        = column[Map[CategoryName, CategoryImportance]]("categories")
      def history: Rep[Map[String, String]] = column[Map[String, String]]("history")

      def historyMapped: MappedProjection[Map[YearMonth, Boolean], Map[String, String]] =
        history.<>(h => h.map(kv => yearMonthIso.comap(kv._1) -> kv._2.toBoolean),
                   h => Option(h.map(kv => yearMonthIso.map(kv._1) -> kv._2.toString)))

      def * = (id, languages, categories, historyMapped)
    }
  }

  object AfterKebs {
    import pl.iterators.kebs.circe.instances.time.YearMonthString
    import pl.iterators.kebs.slick.Kebs

    object MyPostgresProfile extends ExPostgresProfile with PgHStoreSupport {
      override val api: APIWithHstore = new APIWithHstore {}
      trait APIWithHstore extends super.API with HStoreImplicits with Kebs with YearMonthString
    }

    import MyPostgresProfile.api._
    class HStoreTestTable(tag: Tag)
        extends Table[(
            Long,
            Map[LanguageName, LanguageImportance],
            Map[CategoryName, CategoryImportance],
            Map[YearMonth, Boolean]
        )](tag, "HStoreTest") {
      def id                                    = column[Long]("id", O.AutoInc, O.PrimaryKey)
      def languages                             = column[Map[LanguageName, LanguageImportance]]("languages")
      def categories                            = column[Map[CategoryName, CategoryImportance]]("categories")
      def history: Rep[Map[YearMonth, Boolean]] = column[Map[YearMonth, Boolean]]("history")

      def * = (id, languages, categories, history)
    }
  }
}
