package pl.iterators.kebs.slick.hstore

import com.github.tminglei.slickpg._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import slick.lifted.ProvenShape

import java.time.YearMonth
import java.util.UUID

class SlickPgHstoreTests extends AnyFunSuite with Matchers {
  import pl.iterators.kebs.slick.Kebs
  import pl.iterators.kebs.core.macros.CaseClass1ToValueClass._
  import pl.iterators.kebs.instances.time.YearMonthString

  trait PostgresDriver extends ExPostgresProfile with PgArraySupport with PgHStoreSupport {
    override val api: HstoreAPI = new HstoreAPI {}
    trait HstoreAPI extends super.ExtPostgresAPI with ArrayImplicits with HStoreImplicits with Kebs with YearMonthString
  }
  object PostgresDriver extends PostgresDriver

  abstract class BaseTable[T](tag: BaseTable.Tag, tableName: String) extends BaseTable.driver.Table[T](tag, tableName) {
    protected val driver: PostgresDriver = BaseTable.driver
  }

  object BaseTable {
    protected val driver = PostgresDriver
    type Tag = driver.api.Tag
  }

  case class TestId(value: UUID)
  case class TestKey(value: String)
  case class TestValue(value: Int)
  case class Test(id: TestId, hstoreMap: Map[TestKey, TestValue])

  class Tests(tag: BaseTable.Tag) extends BaseTable[Test](tag, "test") {
    import driver.api._

    def id: Rep[TestId]                         = column[TestId]("id")
    def hstoreMap: Rep[Map[TestKey, TestValue]] = column[Map[TestKey, TestValue]]("hstore_map")

    override def * : ProvenShape[Test] = (id, hstoreMap) <> ((Test.apply _).tupled, Test.unapply)
  }

  test("No CaseClass1Rep implicits derived") {
    import pl.iterators.kebs.core.macros.ValueClassLike

    "implicitly[ValueClassLike[YearMonth, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, YearMonth]]" shouldNot typeCheck
  }

  test("Case class hstore extension methods") {
    """
      |    class TestRepository1 {
      |      import PostgresDriver.api._
      |
      |      def getValueForKey(key: TestKey) =
      |        tests.map(_.hstoreMap +> key).result
      |      def exist(key: TestKey) =
      |        tests.map(_.hstoreMap ?? key).result
      |      def defined(key: TestKey) =
      |        tests.map(_.hstoreMap ?* key).result
      |      def existAny(keys: List[TestKey]) =
      |        tests.map(_.hstoreMap ?| keys).result
      |      def existAll(keys: List[TestKey]) =
      |        tests.map(_.hstoreMap ?& keys).result
      |      def contains(submap: Map[TestKey, TestValue]) =
      |        tests.map(_.hstoreMap @> submap).result
      |      def containedBy(supermap: Map[TestKey, TestValue]) =
      |        tests.map(_.hstoreMap.<@:(supermap)).result
      |      def concatenate(map: Map[TestKey, TestValue]) =
      |        tests.map(_.hstoreMap @+ map).result
      |      def deleteMap(map: Map[TestKey, TestValue]) =
      |        tests.map(_.hstoreMap @- map).result
      |      def deleteKeys(keys: List[TestKey]) =
      |        tests.map(_.hstoreMap -- keys).result
      |      def deleteKey(key: TestKey) =
      |        tests.map(_.hstoreMap -/ key).result
      |      def slice(keys: List[TestKey]) =
      |        tests.map(_.hstoreMap slice keys).result
      |
      |      private val tests = TableQuery[Tests]
      |    }
      |""".stripMargin should compile
  }

  case class ObjectTest(id: TestId, hstoreMap: Map[YearMonth, String])

  class ObjectTests(tag: BaseTable.Tag) extends BaseTable[ObjectTest](tag, "test") {
    import driver.api._

    def id: Rep[TestId]                        = column[TestId]("id")
    def hstoreMap: Rep[Map[YearMonth, String]] = column[Map[YearMonth, String]]("hstore_map")

    override def * : ProvenShape[ObjectTest] = (id, hstoreMap) <> ((ObjectTest.apply _).tupled, ObjectTest.unapply)
  }

  test("Standard Java type hstore extension methods") {
    """
      |    class TestRepository1 {
      |      import PostgresDriver.api._
      |
      |      def getValueForKey(key: YearMonth) =
      |        tests.map(_.hstoreMap +> key).result
      |      def exist(key: YearMonth) =
      |        tests.map(_.hstoreMap ?? key).result
      |      def defined(key: YearMonth) =
      |        tests.map(_.hstoreMap ?* key).result
      |      def existAny(keys: List[YearMonth]) =
      |        tests.map(_.hstoreMap ?| keys).result
      |      def existAll(keys: List[YearMonth]) =
      |        tests.map(_.hstoreMap ?& keys).result
      |      def contains(submap: Map[YearMonth, String]) =
      |        tests.map(_.hstoreMap @> submap).result
      |      def containedBy(supermap: Map[YearMonth, String]) =
      |        tests.map(_.hstoreMap.<@:(supermap)).result
      |      def concatenate(map: Map[YearMonth, String]) =
      |        tests.map(_.hstoreMap @+ map).result
      |      def deleteMap(map: Map[YearMonth, String]) =
      |        tests.map(_.hstoreMap @- map).result
      |      def deleteKeys(keys: List[YearMonth]) =
      |        tests.map(_.hstoreMap -- keys).result
      |      def deleteKey(key: YearMonth) =
      |        tests.map(_.hstoreMap -/ key).result
      |      def slice(keys: List[YearMonth]) =
      |        tests.map(_.hstoreMap slice keys).result
      |
      |      private val tests = TableQuery[ObjectTests]
      |    }
      |""".stripMargin should compile
  }
}
