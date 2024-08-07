package pl.iterators.kebs.doobie

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import doobie._
import doobie.implicits._
import doobie.postgres._
import doobie.postgres.implicits._
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.doobie.enums._
import pl.iterators.kebs.doobie._
import pl.iterators.kebs.instances.KebsInstances._
import pl.iterators.kebs.doobie.model._

import java.util.Currency

class ComplexTypesTests
    extends AnyFunSuite
    with Matchers
    with KebsEnumsForTests
    with CaseClass1ToValueClass
    with KebsDoobieEnums
    with KebsDoobieValueEnums {

  test("Put & Get exist") {
    "implicitly[Get[Name]]" should compile
    "implicitly[Put[Name]]" should compile
    "implicitly[Get[List[Name]]]" should compile
    "implicitly[Put[List[Name]]]" should compile
    "implicitly[Get[Array[Name]]]" should compile
    "implicitly[Put[Array[Name]]]" should compile
    "implicitly[Get[Vector[Name]]]" should compile
    "implicitly[Put[Vector[Name]]]" should compile
    "implicitly[Get[List[Option[Name]]]]" should compile
    "implicitly[Put[List[Option[Name]]]]" should compile
    "implicitly[Get[Array[Option[Name]]]]" should compile
    "implicitly[Put[Array[Option[Name]]]]" should compile
    "implicitly[Get[Vector[Option[Name]]]]" should compile
    "implicitly[Put[Vector[Option[Name]]]]" should compile

    "implicitly[Get[Currency]]" should compile
    "implicitly[Put[Currency]]" should compile
    "implicitly[Get[List[Currency]]]" should compile
    "implicitly[Put[List[Currency]]]" should compile
    "implicitly[Get[Array[Currency]]]" should compile
    "implicitly[Put[Array[Currency]]]" should compile
    "implicitly[Get[Vector[Currency]]]" should compile
    "implicitly[Put[Vector[Currency]]]" should compile
    "implicitly[Get[List[Option[Currency]]]]" should compile
    "implicitly[Put[List[Option[Currency]]]]" should compile
    "implicitly[Get[Array[Option[Currency]]]]" should compile
    "implicitly[Put[Array[Option[Currency]]]]" should compile
    "implicitly[Get[Vector[Option[Currency]]]]" should compile
    "implicitly[Put[Vector[Option[Currency]]]]" should compile

    "implicitly[Get[EyeColor]]" should compile
    "implicitly[Put[EyeColor]]" should compile
    "implicitly[Get[List[EyeColor]]]" should compile
    "implicitly[Put[List[EyeColor]]]" should compile
    "implicitly[Get[Array[EyeColor]]]" should compile
    "implicitly[Put[Array[EyeColor]]]" should compile
    "implicitly[Get[Vector[EyeColor]]]" should compile
    "implicitly[Put[Vector[EyeColor]]]" should compile
    "implicitly[Get[List[Option[EyeColor]]]]" should compile
    "implicitly[Put[List[Option[EyeColor]]]]" should compile
    "implicitly[Get[Array[Option[EyeColor]]]]" should compile
    "implicitly[Put[Array[Option[EyeColor]]]]" should compile
    "implicitly[Get[Vector[Option[EyeColor]]]]" should compile
    "implicitly[Put[Vector[Option[EyeColor]]]]" should compile

    "implicitly[Get[LibraryItem]]" should compile
    "implicitly[Put[LibraryItem]]" should compile
    "implicitly[Get[List[LibraryItem]]]" should compile
    "implicitly[Put[List[LibraryItem]]]" should compile
    "implicitly[Get[Array[LibraryItem]]]" should compile
    "implicitly[Put[Array[LibraryItem]]]" should compile
    "implicitly[Get[Vector[LibraryItem]]]" should compile
    "implicitly[Put[Vector[LibraryItem]]]" should compile
    "implicitly[Get[List[Option[LibraryItem]]]]" should compile
    "implicitly[Put[List[Option[LibraryItem]]]]" should compile
    "implicitly[Get[Array[Option[LibraryItem]]]]" should compile
    "implicitly[Put[Array[Option[LibraryItem]]]]" should compile
    "implicitly[Get[Vector[Option[LibraryItem]]]]" should compile
    "implicitly[Put[Vector[Option[LibraryItem]]]]" should compile
  }

  test("Query should compile") {
    """sql"SELECT * FROM people".query[Person].unique""" should compile
  }
}
