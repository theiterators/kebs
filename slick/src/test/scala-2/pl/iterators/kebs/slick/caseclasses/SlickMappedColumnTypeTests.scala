package pl.iterators.kebs.slick.caseclasses

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.slick.KebsSlickSupport
import slick.jdbc.PostgresProfile

class SlickMappedColumnTypeTests extends AnyFunSuite with Matchers {
  object MyPostgresProfile extends PostgresProfile with KebsSlickSupport {
    override val api: APITagged = new APITagged {}
    trait APITagged extends JdbcAPI with KebsBasicImplicits with KebsValueClassLikeImplicits with CaseClass1ToValueClass
  }

  import MyPostgresProfile.api._
  import slick.lifted.ProvenShape

  case class Id(id: Long)
  case class Row(id: Id, name: String, num: Long)
  case class Name(name: String)
  case class WrappedName(name: Name)

  test("MappedColumnType for case classes") {
    "implicitly[BaseColumnType[Id]]" should compile
  }

  test("Slick mapping") {
    """
      |class ATable(tag: Tag) extends Table[(Id, String, Long)](tag, "A_TABLE") {
      |      def id   = column[Id]("id")
      |      def name = column[String]("name")
      |      def num  = column[Long]("num")
      |
      |      override def * = (id, name, num)
      |}
    """.stripMargin should compile
  }

  test("Slick mapping - mapped projection") {
    """
      |
      |class ATable(tag: Tag) extends Table[Row](tag, "A_TABLE") {
      |      def id   = column[Id]("id")
      |      def name = column[String]("name")
      |      def num  = column[Long]("num")
      |
      |      override def * : ProvenShape[Row] = (id, name, num) <> ((Row.apply _).tupled, Row.unapply)
      |    }
    """.stripMargin should compile
  }

  // for the tests below, Name.unapply (etc.) works fine, Scala 3 is more picky here

  test("Slick mapping - one element wrapper") {
    """
      |class OneElement(tag: Tag) extends Table[Name](tag, "ONE_ELEMENT_TABLE") {
      |      def name                           = column[String]("name")
      |      
      |      override def * : ProvenShape[Name] = name <> (Name.apply, Name.unapply)
      |    }
    """.stripMargin should compile
  }

  test("Slick mapping - matryoshka case 1") {
    """
      |class Matryoshka(tag: Tag) extends Table[WrappedName](tag, "MATRYOSHKA") {
      |      def name                                  = column[Name]("name")
      |
      |      override def * : ProvenShape[WrappedName] = name <> (WrappedName.apply, WrappedName.unapply)
      |}
    """.stripMargin should compile
  }

  test("Slick mapping - matryoshka case 2") {
    """
      |class Matryoshka(tag: Tag) extends Table[WrappedName](tag, "MATRYOSHKA") {
      |      def name                                  = column[Name]("name")
      |      private def mappedProjection              = name <> (WrappedName.apply, WrappedName.unapply)
      |      
      |      override def * : ProvenShape[WrappedName] = mappedProjection
      |    }
    """.stripMargin should compile
  }

  class NoMapping(id: Long)

  test("Wrong slick mapping") {
    """
      |class ATable(tag: Tag) extends Table[(NoMapping, String)](tag, "A_TABLE") {
      |      def id   = column[NoMapping]("id")
      |      def name = column[String]("name")
      |
      |      override def * = (id, name)
      |}
    """.stripMargin shouldNot typeCheck
  }
}
