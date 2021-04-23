package pl.iterators.kebs

import pl.iterators.kebs.hstore.KebsHStoreColumnExtensionMethods
import pl.iterators.kebs.macros.CaseClass1Rep
import slick.ast.{BaseTypedType, NumericTypedType}
import slick.jdbc.JdbcType
import slick.lifted._

import scala.language.implicitConversions

trait KebsColumnExtensionMethods {

  /** Case class column extension methods */
  implicit def stringValueColumnExt[CC](rep: Rep[CC])(implicit ev: CaseClass1Rep[CC, String]): StringColumnExtensionMethods[CC] =
    new StringColumnExtensionMethods[CC](rep)
  implicit def stringValueOptionColumnExt[CC](rep: Rep[Option[CC]])(
      implicit ev: CaseClass1Rep[CC, String]): StringColumnExtensionMethods[Option[CC]] = new StringColumnExtensionMethods[Option[CC]](rep)
  implicit def numericValueColumnExt[CC, B](rep: Rep[CC])(
      implicit ev1: CaseClass1Rep[CC, B],
      ev2: BaseTypedType[B] with NumericTypedType): BaseNumericColumnExtensionMethods[CC] = new BaseNumericColumnExtensionMethods[CC](rep)
  implicit def numericValueOptionColumnExt[CC, B](rep: Rep[Option[CC]])(
      implicit ev1: CaseClass1Rep[CC, B],
      ev2: BaseTypedType[B] with NumericTypedType): OptionNumericColumnExtensionMethods[CC] =
    new OptionNumericColumnExtensionMethods[CC](rep)
  implicit def booleanValueColumnExt[CC](rep: Rep[CC])(implicit ev: CaseClass1Rep[CC, Boolean]): BooleanColumnExtensionMethods[CC] =
    new BooleanColumnExtensionMethods[CC](rep)
  implicit def booleanValueOptionColumnExt[CC](rep: Rep[Option[CC]])(
      implicit ev: CaseClass1Rep[CC, Boolean]): BooleanColumnExtensionMethods[Option[CC]] =
    new BooleanColumnExtensionMethods[Option[CC]](rep)

  implicit def simpleHstoreColumnExtensionMethods[KEY, VALUE](c: Rep[Map[KEY, VALUE]])(
      implicit tm0: JdbcType[KEY],
      tm1: JdbcType[VALUE],
      tm2: JdbcType[List[KEY]],
      tm3: JdbcType[List[VALUE]],
      tm4: JdbcType[Map[KEY, VALUE]]
  ) = new KebsHStoreColumnExtensionMethods[KEY, VALUE, Map[KEY, VALUE]](c)

  @inline implicit def getCCOptionMapper2TT_1[B1, B2: BaseTypedType, BR, CC](
      implicit ev: CaseClass1Rep[CC, B1]): OptionMapper2[B1, B2, BR, CC, B2, BR] =
    OptionMapper2.plain.asInstanceOf[OptionMapper2[B1, B2, BR, CC, B2, BR]]
  @inline implicit def getCCOptionMapper2TT_2[B1, B2, BR, CC](implicit ev: CaseClass1Rep[CC, B2]): OptionMapper2[CC, CC, BR, CC, B2, BR] =
    OptionMapper2.plain.asInstanceOf[OptionMapper2[CC, CC, BR, CC, B2, BR]]
  @inline implicit def getCCOptionMapper2TO[B1, B2: BaseTypedType, BR, CC](
      implicit ev: CaseClass1Rep[CC, B1]): OptionMapper2[B1, B2, BR, CC, Option[B2], Option[BR]] =
    OptionMapper2.option.asInstanceOf[OptionMapper2[B1, B2, BR, CC, Option[B2], Option[BR]]]
  @inline implicit def getCCOptionMapper2OT[B1, B2: BaseTypedType, BR, CC](
      implicit ev: CaseClass1Rep[CC, B1]): OptionMapper2[B1, B2, BR, Option[CC], B2, Option[BR]] =
    OptionMapper2.option.asInstanceOf[OptionMapper2[B1, B2, BR, Option[CC], B2, Option[BR]]]
  @inline implicit def getCCOptionMapper2OO[B1, B2: BaseTypedType, BR, CC](
      implicit ev: CaseClass1Rep[CC, B1]): OptionMapper2[B1, B2, BR, Option[CC], Option[B2], Option[BR]] =
    OptionMapper2.option.asInstanceOf[OptionMapper2[B1, B2, BR, Option[CC], Option[B2], Option[BR]]]
}

trait Kebs extends KebsColumnExtensionMethods {

  /** Case class isomorphism */
  implicit def valueColumnType[CC, B](implicit rep1: CaseClass1Rep[CC, B]): Isomorphism[CC, B] =
    new Isomorphism[CC, B](rep1.unapply, rep1.apply)

  /** List isomorphisms */
  implicit def listStringValueColumnType[A](implicit iso: Isomorphism[A, String]): Isomorphism[List[A], List[String]] = {
    new Isomorphism[List[A], List[String]](_.map(iso.map), _.map(iso.comap))
  }

  implicit def listBooleanValueColumnType[A](implicit iso: Isomorphism[A, Boolean]): Isomorphism[List[A], List[Boolean]] = {
    new Isomorphism[List[A], List[Boolean]](_.map(iso.map), _.map(iso.comap))
  }

  implicit def listIntValueColumnType[A, Int](implicit iso: Isomorphism[A, Int]): Isomorphism[List[A], List[Int]] = {
    new Isomorphism[List[A], List[Int]](_.map(iso.map), _.map(iso.comap))
  }

  implicit def listLongValueColumnType[A, Int](implicit iso: Isomorphism[A, Long]): Isomorphism[List[A], List[Long]] = {
    new Isomorphism[List[A], List[Long]](_.map(iso.map), _.map(iso.comap))
  }

  /** Seq isomorphism */
  implicit def seqValueColumnType[CC <: Product, B](implicit iso: Isomorphism[CC, B]): Isomorphism[Seq[CC], List[B]] = {
    new Isomorphism[Seq[CC], List[B]](_.map(iso.map).toList, _.map(iso.comap))
  }

  /** Map isomorphisms */
  implicit def mapValueColumnType[CC1 <: Product, CC2 <: Product, A, B](
      implicit iso1: Isomorphism[CC1, A],
      iso2: Isomorphism[CC2, B]
  ): Isomorphism[Map[CC1, CC2], Map[A, B]] =
    new Isomorphism[Map[CC1, CC2], Map[A, B]](
      _.map { case (cc1, cc2) => (iso1.map(cc1), iso2.map(cc2)) },
      _.map { case (a, b)     => (iso1.comap(a), iso2.comap(b)) }
    )

  private class StringMapIsomorphism[A](comap: String => A)
      extends Isomorphism[Map[String, A], Map[String, String]](
        _.map { case (str, a)     => (str, a.toString) },
        _.map { case (str1, str2) => (str1, comap(str2)) }
      )
  implicit final val intMapValueColumnType: Isomorphism[Map[String, Int], Map[String, String]]   = new StringMapIsomorphism[Int](_.toInt)
  implicit final val longMapValueColumnType: Isomorphism[Map[String, Long], Map[String, String]] = new StringMapIsomorphism[Long](_.toLong)
  implicit final val boolMapValueColumnType: Isomorphism[Map[String, Boolean], Map[String, String]] =
    new StringMapIsomorphism[Boolean](_.toBoolean)

  /** Hstore isomorphisms */
  implicit def hstoreColumnType[CC1 <: Product, CC2 <: Product, A](
      implicit iso1: Isomorphism[Map[CC1, CC2], Map[String, A]],
      iso2: Isomorphism[Map[String, A], Map[String, String]]): Isomorphism[Map[CC1, CC2], Map[String, String]] =
    new Isomorphism[Map[CC1, CC2], Map[String, String]](
      iso1.map andThen iso2.map,
      iso2.comap andThen iso1.comap
    )

  implicit def hstoreColumnStringValue[A](implicit iso1: Isomorphism[A, String]): Isomorphism[Map[A, String], Map[String, String]] =
    new Isomorphism[Map[A, String], Map[String, String]](
      _.map { case (a, str)     => (iso1.map(a), str) },
      _.map { case (str1, str2) => (iso1.comap(str1), str2) }
    )

  implicit def hstoreColumnIntValue[A](implicit iso1: Isomorphism[A, String]): Isomorphism[Map[A, Int], Map[String, Int]] =
    new Isomorphism[Map[A, Int], Map[String, Int]](
      _.map { case (a, int)   => (iso1.map(a), int) },
      _.map { case (str, int) => (iso1.comap(str), int) }
    )

  implicit def hstoreColumnLongValue[A](implicit iso1: Isomorphism[A, String]): Isomorphism[Map[A, Long], Map[String, Long]] =
    new Isomorphism[Map[A, Long], Map[String, Long]](
      _.map { case (a, long)   => (iso1.map(a), long) },
      _.map { case (str, long) => (iso1.comap(str), long) }
    )

  implicit def hstoreColumnBooleanValue[A](implicit iso1: Isomorphism[A, String]): Isomorphism[Map[A, Boolean], Map[String, Boolean]] =
    new Isomorphism[Map[A, Boolean], Map[String, Boolean]](
      _.map { case (a, bool)   => (iso1.map(a), bool) },
      _.map { case (str, bool) => (iso1.comap(str), bool) }
    )
}
