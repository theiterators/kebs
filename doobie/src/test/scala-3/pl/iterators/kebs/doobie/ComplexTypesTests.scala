package pl.iterators.kebs.doobie

import doobie.implicits.given
import doobie.postgres.given
import doobie.postgres.implicits.given
import doobie._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.doobie.model._
import pl.iterators.kebs.enums.KebsEnum
import pl.iterators.kebs.doobie.given 
import pl.iterators.kebs.doobie.enums.given 
import pl.iterators.kebs.instances.KebsInstances.given
import pl.iterators.kebs.opaque.Opaque

import java.util.Currency

class ComplexTypesTests extends AnyFunSuite with Matchers with KebsEnum {
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
  }

  test("Query should compile") {
    """sql"SELECT * FROM people".query[Person].unique""" should compile
  }
}
