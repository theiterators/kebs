package pl.iterators.kebs

import pl.iterators.kebs.hstore.KebsHStoreColumnExtensionMethods
import pl.iterators.kebs.macros.CaseClass1Rep
import slick.ast.{BaseTypedType, NumericTypedType}
import slick.jdbc.JdbcType
import slick.lifted._

import scala.language.implicitConversions

trait KebsColumnExtensionMethods {
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
  implicit def valueColumnType[CC, B](implicit rep1: CaseClass1Rep[CC, B]): Isomorphism[CC, B] =
    new Isomorphism[CC, B](rep1.unapply, rep1.apply)
  implicit def listValueColumnType[CC, B](implicit iso: Isomorphism[CC, B]): Isomorphism[List[CC], List[B]] =
    new Isomorphism[List[CC], List[B]](_.map(iso.map), _.map(iso.comap))
  implicit def seqValueColumnType[CC, B](implicit iso: Isomorphism[CC, B]): Isomorphism[Seq[CC], List[B]] = {
    new Isomorphism[Seq[CC], List[B]](_.map(iso.map).toList, _.map(iso.comap))
  }
  implicit def mapValueColumnType[CC1, CC2, A, B](
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
  implicit final val intMapValueColumnType: Isomorphism[Map[String, Int], Map[String, String]] =
    new StringMapIsomorphism[Int](_.toInt)
  implicit final val longMapValueColumnType: Isomorphism[Map[String, Long], Map[String, String]] =
    new StringMapIsomorphism[Long](_.toLong)
  implicit final val boolMapValueColumnType: Isomorphism[Map[String, Boolean], Map[String, String]] =
    new StringMapIsomorphism[Boolean](_.toBoolean)

  private class StringValueMapIsomorphism[A](comap: String => A)
      extends Isomorphism[Map[A, String], Map[String, String]](
        _.map { case (a, str)     => (a.toString, str) },
        _.map { case (str1, str2) => (comap(str1), str2) }
      )
  implicit final val intMapValueColumnType1: Isomorphism[Map[Int, String], Map[String, String]] =
    new StringValueMapIsomorphism[Int](_.toInt)
  implicit final val longMapValueColumnType1: Isomorphism[Map[Long, String], Map[String, String]] =
    new StringValueMapIsomorphism[Long](_.toLong)
  implicit final val boolMapValueColumnType1: Isomorphism[Map[Boolean, String], Map[String, String]] =
    new StringValueMapIsomorphism[Boolean](_.toBoolean)

  implicit def hstoreColumnType[CC1, CC2, A, B](
      implicit iso1: Isomorphism[Map[CC1, CC2], Map[A, B]],
      iso2: Isomorphism[Map[A, B], Map[String, String]]): Isomorphism[Map[CC1, CC2], Map[String, String]] =
    new Isomorphism[Map[CC1, CC2], Map[String, String]](
      iso1.map andThen iso2.map,
      iso2.comap andThen iso1.comap
    )

  def cc1repIsoMapVal[Obj, Value, MapVal](comap1: String => Value, comap2: String => MapVal)(implicit cc1rep: CaseClass1Rep[Obj, Value]) =
    new Isomorphism[Map[Obj, MapVal], Map[String, String]](
      _.map { case (obj, mapval) => (cc1rep.unapply(obj).toString, mapval.toString) },
      _.map { case (value, str)  => (cc1rep.apply(comap1(value)), comap2(str)) }
    )
  def cc1repIsoMapKey[Obj, Value, MapKey](comap1: String => Value, comap2: String => MapKey)(implicit cc1rep: CaseClass1Rep[Obj, Value]) =
    new Isomorphism[Map[MapKey, Obj], Map[String, String]](
      _.map { case (mapkey, obj) => (mapkey.toString, cc1rep.unapply(obj).toString) },
      _.map { case (str1, str2)  => (comap2(str1), cc1rep.apply(comap1(str2))) }
    )

  implicit def cc1repIsoObj2Str[Obj](implicit cc1rep: CaseClass1Rep[Obj, String]): Isomorphism[Map[Obj, String], Map[String, String]] =
    cc1repIsoMapVal[Obj, String, String](identity[String], identity[String])
  implicit def cc1repIsoObj2Str2[Obj](implicit cc1rep: CaseClass1Rep[Obj, String]): Isomorphism[Map[String, Obj], Map[String, String]] =
    cc1repIsoMapKey[Obj, String, String](identity[String], identity[String])
  implicit def cc1repIsoObj2Str3[Obj](implicit cc1rep: CaseClass1Rep[Obj, String]): Isomorphism[Map[Obj, Int], Map[String, String]] =
    cc1repIsoMapVal[Obj, String, Int](identity[String], _.toInt)
  implicit def cc1repIsoObj2Str4[Obj](implicit cc1rep: CaseClass1Rep[Obj, String]): Isomorphism[Map[Int, Obj], Map[String, String]] =
    cc1repIsoMapKey[Obj, String, Int](identity[String], _.toInt)
  implicit def cc1repIsoObj2Str5[Obj](implicit cc1rep: CaseClass1Rep[Obj, String]): Isomorphism[Map[Obj, Long], Map[String, String]] =
    cc1repIsoMapVal[Obj, String, Long](identity[String], _.toLong)
  implicit def cc1repIsoObj2Str6[Obj](implicit cc1rep: CaseClass1Rep[Obj, String]): Isomorphism[Map[Long, Obj], Map[String, String]] =
    cc1repIsoMapKey[Obj, String, Long](identity[String], _.toLong)
  implicit def cc1repIsoObj2Str7[Obj](implicit cc1rep: CaseClass1Rep[Obj, String]): Isomorphism[Map[Obj, Boolean], Map[String, String]] =
    cc1repIsoMapVal[Obj, String, Boolean](identity[String], _.toBoolean)
  implicit def cc1repIsoObj2Str8[Obj](implicit cc1rep: CaseClass1Rep[Obj, String]): Isomorphism[Map[Boolean, Obj], Map[String, String]] =
    cc1repIsoMapKey[Obj, String, Boolean](identity[String], _.toBoolean)

  implicit def cc1repIsoObj2Int[Obj](implicit cc1rep: CaseClass1Rep[Obj, Int]): Isomorphism[Map[Obj, String], Map[String, String]] =
    cc1repIsoMapVal[Obj, Int, String](_.toInt, identity[String])
  implicit def cc1repIsoObj2Int1[Obj](implicit cc1rep: CaseClass1Rep[Obj, Int]): Isomorphism[Map[String, Obj], Map[String, String]] =
    cc1repIsoMapKey[Obj, Int, String](_.toInt, identity[String])
  implicit def cc1repIsoObj2Int2[Obj](implicit cc1rep: CaseClass1Rep[Obj, Int]): Isomorphism[Map[Obj, Int], Map[String, String]] =
    cc1repIsoMapVal[Obj, Int, Int](_.toInt, _.toInt)
  implicit def cc1repIsoObj2Int3[Obj](implicit cc1rep: CaseClass1Rep[Obj, Int]): Isomorphism[Map[Int, Obj], Map[String, String]] =
    cc1repIsoMapKey[Obj, Int, Int](_.toInt, _.toInt)
  implicit def cc1repIsoObj2Int4[Obj](implicit cc1rep: CaseClass1Rep[Obj, Int]): Isomorphism[Map[Obj, Long], Map[String, String]] =
    cc1repIsoMapVal[Obj, Int, Long](_.toInt, _.toLong)
  implicit def cc1repIsoObj2Int5[Obj](implicit cc1rep: CaseClass1Rep[Obj, Int]): Isomorphism[Map[Long, Obj], Map[String, String]] =
    cc1repIsoMapKey[Obj, Int, Long](_.toInt, _.toLong)
  implicit def cc1repIsoObj2Int6[Obj](implicit cc1rep: CaseClass1Rep[Obj, Int]): Isomorphism[Map[Obj, Boolean], Map[String, String]] =
    cc1repIsoMapVal[Obj, Int, Boolean](_.toInt, _.toBoolean)
  implicit def cc1repIsoObj2Int7[Obj](implicit cc1rep: CaseClass1Rep[Obj, Int]): Isomorphism[Map[Boolean, Obj], Map[String, String]] =
    cc1repIsoMapKey[Obj, Int, Boolean](_.toInt, _.toBoolean)

  implicit def cc1repIsoObj2Long[Obj](implicit cc1rep: CaseClass1Rep[Obj, Long]): Isomorphism[Map[Obj, String], Map[String, String]] =
    cc1repIsoMapVal[Obj, Long, String](_.toLong, identity[String])
  implicit def cc1repIsoObj2Long1[Obj](implicit cc1rep: CaseClass1Rep[Obj, Long]): Isomorphism[Map[String, Obj], Map[String, String]] =
    cc1repIsoMapKey[Obj, Long, String](_.toLong, identity[String])
  implicit def cc1repIsoObj2Long2[Obj](implicit cc1rep: CaseClass1Rep[Obj, Long]): Isomorphism[Map[Obj, Int], Map[String, String]] =
    cc1repIsoMapVal[Obj, Long, Int](_.toLong, _.toInt)
  implicit def cc1repIsoObj2Long3[Obj](implicit cc1rep: CaseClass1Rep[Obj, Long]): Isomorphism[Map[Int, Obj], Map[String, String]] =
    cc1repIsoMapKey[Obj, Long, Int](_.toLong, _.toInt)
  implicit def cc1repIsoObj2Long4[Obj](implicit cc1rep: CaseClass1Rep[Obj, Long]): Isomorphism[Map[Obj, Long], Map[String, String]] =
    cc1repIsoMapVal[Obj, Long, Long](_.toLong, _.toLong)
  implicit def cc1repIsoObj2Long5[Obj](implicit cc1rep: CaseClass1Rep[Obj, Long]): Isomorphism[Map[Long, Obj], Map[String, String]] =
    cc1repIsoMapKey[Obj, Long, Long](_.toLong, _.toLong)
  implicit def cc1repIsoObj2Long6[Obj](implicit cc1rep: CaseClass1Rep[Obj, Long]): Isomorphism[Map[Obj, Boolean], Map[String, String]] =
    cc1repIsoMapVal[Obj, Long, Boolean](_.toLong, _.toBoolean)
  implicit def cc1repIsoObj2Long7[Obj](implicit cc1rep: CaseClass1Rep[Obj, Long]): Isomorphism[Map[Boolean, Obj], Map[String, String]] =
    cc1repIsoMapKey[Obj, Long, Boolean](_.toLong, _.toBoolean)
}
