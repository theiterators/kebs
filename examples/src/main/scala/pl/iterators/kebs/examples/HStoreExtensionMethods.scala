package pl.iterators.kebs.examples

import com.github.tminglei.slickpg.{ExPostgresProfile, PgArraySupport, PgHStoreSupport}
import slick.lifted.MappedProjection

import java.time.YearMonth
import scala.concurrent.ExecutionContext

object HStoreExtensionMethods {
  object BeforeKebs {

    object MyPostgresProfile extends ExPostgresProfile with PgHStoreSupport with PgArraySupport {
      override val api: APIWithHstore = new APIWithHstore {}
      trait APIWithHstore extends super.API with HStoreImplicits with ArrayImplicits {
        val yearMonthIso: Isomorphism[YearMonth, String] = new Isomorphism(_.toString, YearMonth.parse)
      }
    }

    import MyPostgresProfile.api._

    class HStoreTestTable(tag: Tag) extends Table[(Long, Map[YearMonth, Boolean])](tag, "HStoreTest") {
      def id                                = column[Long]("id")
      def history: Rep[Map[String, String]] = column[Map[String, String]]("history")

      def historyMapped: MappedProjection[Map[YearMonth, Boolean], Map[String, String]] =
        history.<>(h => h.map(kv => yearMonthIso.comap(kv._1) -> kv._2.toBoolean),
                   h => Option(h.map(kv => yearMonthIso.map(kv._1) -> kv._2.toString)))

      def * = (id, historyMapped)
    }

    class HstoreRepository(implicit ec: ExecutionContext) {

      def get(id: Long, yearMonth: YearMonth): DBIO[Option[Boolean]] =
        byIdQuery(id)
          .map(_.history +> yearMonthIso.map(yearMonth).asColumnOf[Option[String]])
          .result
          .map(_.headOption.flatMap(_.map(_.toBoolean)))

      private def byIdQuery(id: Long) = testTable.filter(_.id === id)

      private def update(id: Long, newHistoryMap: Map[String, String]): DBIO[Int] =
        byIdQuery(id).map(_.history).update(newHistoryMap)

      private val testTable = TableQuery[HStoreTestTable]
    }

  }

  object AfterKebs {
    import pl.iterators.kebs.circe.instances.time.YearMonthString
    import pl.iterators.kebs.slick.Kebs

    object MyPostgresProfile extends ExPostgresProfile with PgHStoreSupport with PgArraySupport {
      override val api: APIWithHstore = new APIWithHstore {}
      trait APIWithHstore extends super.API with HStoreImplicits with ArrayImplicits with Kebs with YearMonthString
    }

    import MyPostgresProfile.api._

    class HStoreTestTable(tag: Tag) extends Table[(Long, Map[YearMonth, Boolean])](tag, "HStoreTest") {
      def id                                    = column[Long]("id")
      def history: Rep[Map[YearMonth, Boolean]] = column[Map[YearMonth, Boolean]]("history")

      def * = (id, history)
    }

    class HstoreRepository(implicit ec: ExecutionContext) {

      def get(id: Long, yearMonth: YearMonth): DBIO[Option[Boolean]] =
        byIdQuery(id)
          .map(_.history +> yearMonth)
          .result
          .map(_.headOption.flatten)

      private def byIdQuery(id: Long) = testTable.filter(_.id === id)

      private def update(id: Long, newHistoryMap: Map[YearMonth, Boolean]): DBIO[Int] =
        byIdQuery(id).map(_.history).update(newHistoryMap)

      private val testTable = TableQuery[HStoreTestTable]
    }
  }
}
