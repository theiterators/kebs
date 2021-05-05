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

  implicit def hstoreColumnExt[KEY, VALUE](c: Rep[Map[KEY, VALUE]])(
      implicit tm0: JdbcType[KEY],
      tm1: JdbcType[VALUE],
      tm2: JdbcType[List[KEY]],
      tm3: JdbcType[List[VALUE]],
      tm4: JdbcType[Map[KEY, VALUE]]
  ): KebsHStoreColumnExtensionMethods[KEY, VALUE, Map[KEY, VALUE]] =
    new KebsHStoreColumnExtensionMethods[KEY, VALUE, Map[KEY, VALUE]](c)

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

  /** List isomorphism */
  implicit def listValueColumnType[CC, B](implicit iso: Isomorphism[CC, B]): Isomorphism[List[CC], List[B]] =
    new Isomorphism[List[CC], List[B]](_.map(iso.map), _.map(iso.comap))

  /** Seq isomorphism */
  implicit def seqValueColumnType[CC, B](implicit iso: Isomorphism[CC, B]): Isomorphism[Seq[CC], List[B]] =
    new Isomorphism[Seq[CC], List[B]](_.map(iso.map).toList, _.map(iso.comap))

  /** Map isomorphisms */
  implicit def mapColumnType[CC1, CC2, A, B](
      implicit iso1: Isomorphism[CC1, A],
      iso2: Isomorphism[CC2, B]
  ): Isomorphism[Map[CC1, CC2], Map[A, B]] =
    new Isomorphism[Map[CC1, CC2], Map[A, B]](
      _.map { case (cc1, cc2) => (iso1.map(cc1), iso2.map(cc2)) },
      _.map { case (a, b)     => (iso1.comap(a), iso2.comap(b)) }
    )

  implicit def mapColumnType1[CC, A](
      implicit iso1: Isomorphism[CC, A]
  ): Isomorphism[Map[CC, A], Map[A, A]] =
    new Isomorphism[Map[CC, A], Map[A, A]](
      _.map { case (cc1, a) => (iso1.map(cc1), a) },
      _.map { case (a1, a2) => (iso1.comap(a1), a2) }
    )

  implicit def mapColumnType2[CC, A](
      implicit iso1: Isomorphism[CC, A]
  ): Isomorphism[Map[A, CC], Map[A, A]] =
    new Isomorphism[Map[A, CC], Map[A, A]](
      _.map { case (a, cc)  => (a, iso1.map(cc)) },
      _.map { case (a1, a2) => (a1, iso1.comap(a2)) }
    )

  implicit def mapColumnType3[CC, A, B](
      implicit iso1: Isomorphism[CC, A]
  ): Isomorphism[Map[CC, B], Map[A, B]] =
    new Isomorphism[Map[CC, B], Map[A, B]](
      _.map { case (cc, b) => (iso1.map(cc), b) },
      _.map { case (a, b)  => (iso1.comap(a), b) }
    )

  private class StringMapKeyIsomorphism[A](comap: String => A)
      extends Isomorphism[Map[String, A], Map[String, String]](
        _.map { case (str, a)     => (str, a.toString) },
        _.map { case (str1, str2) => (str1, comap(str2)) }
      )

  implicit final val intMapValueColumnType: Isomorphism[Map[String, Int], Map[String, String]] =
    new StringMapKeyIsomorphism[Int](_.toInt)
  implicit final val longMapValueColumnType: Isomorphism[Map[String, Long], Map[String, String]] =
    new StringMapKeyIsomorphism[Long](_.toLong)
  implicit final val boolMapValueColumnType: Isomorphism[Map[String, Boolean], Map[String, String]] =
    new StringMapKeyIsomorphism[Boolean](_.toBoolean)

  private class StringMapValueIsomorphism[A](comap: String => A)
      extends Isomorphism[Map[A, String], Map[String, String]](
        _.map { case (a, str)     => (a.toString, str) },
        _.map { case (str1, str2) => (comap(str1), str2) }
      )

  implicit final val intMapKeyValueColumnType: Isomorphism[Map[Int, String], Map[String, String]] =
    new StringMapValueIsomorphism[Int](_.toInt)
  implicit final val longMapKeyValueColumnType: Isomorphism[Map[Long, String], Map[String, String]] =
    new StringMapValueIsomorphism[Long](_.toLong)
  implicit final val boolMapKeyValueColumnType: Isomorphism[Map[Boolean, String], Map[String, String]] =
    new StringMapValueIsomorphism[Boolean](_.toBoolean)
}
