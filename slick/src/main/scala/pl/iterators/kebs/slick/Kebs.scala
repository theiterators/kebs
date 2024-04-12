package pl.iterators.kebs.slick

import com.github.tminglei.slickpg.PgCompositeSupport
import pl.iterators.kebs.slick.hstore.KebsHStoreColumnExtensionMethods
import pl.iterators.kebs.core.instances.InstanceConverter
import pl.iterators.kebs.core.macros.{CaseClass1ToValueClass, ValueClassLike}
import pl.iterators.kebs.slick.types.{GenericJdbcType, ListJdbcType, MapJdbcType}
import slick.ast.{BaseTypedType, FieldSymbol, NumericTypedType, ScalaType}
import slick.jdbc.{JdbcProfile, JdbcType, JdbcTypesComponent}
import slick.lifted._
import slick.relational.RelationalProfile

import java.sql.{Array, PreparedStatement, ResultSet}
import scala.language.{implicitConversions, reflectiveCalls}
import scala.reflect.ClassTag

trait KebsColumnExtensionMethods {
  implicit def stringValueColumnExt[CC](rep: Rep[CC])(implicit ev: ValueClassLike[CC, String]): StringColumnExtensionMethods[CC] =
    new StringColumnExtensionMethods[CC](rep)

  implicit def stringValueOptionColumnExt[CC](rep: Rep[Option[CC]])(
    implicit ev: ValueClassLike[CC, String]): StringColumnExtensionMethods[Option[CC]] = new StringColumnExtensionMethods[Option[CC]](rep)

  implicit def numericValueColumnExt[CC, B](rep: Rep[CC])(
    implicit ev1: ValueClassLike[CC, B],
    ev2: BaseTypedType[B] with NumericTypedType): BaseNumericColumnExtensionMethods[CC] = new BaseNumericColumnExtensionMethods[CC](rep)

  implicit def numericValueOptionColumnExt[CC, B](rep: Rep[Option[CC]])(
    implicit ev1: ValueClassLike[CC, B],
    ev2: BaseTypedType[B] with NumericTypedType): OptionNumericColumnExtensionMethods[CC] =
    new OptionNumericColumnExtensionMethods[CC](rep)

  implicit def booleanValueColumnExt[CC](rep: Rep[CC])(implicit ev: ValueClassLike[CC, Boolean]): BooleanColumnExtensionMethods[CC] =
    new BooleanColumnExtensionMethods[CC](rep)

  implicit def booleanValueOptionColumnExt[CC](rep: Rep[Option[CC]])(
    implicit ev: ValueClassLike[CC, Boolean]): BooleanColumnExtensionMethods[Option[CC]] =
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
                                                                              implicit ev: ValueClassLike[CC, B1]): OptionMapper2[B1, B2, BR, CC, B2, BR] =
    OptionMapper2.plain.asInstanceOf[OptionMapper2[B1, B2, BR, CC, B2, BR]]

  @inline implicit def getCCOptionMapper2TT_2[B1, B2, BR, CC](implicit ev: ValueClassLike[CC, B2]): OptionMapper2[CC, CC, BR, CC, B2, BR] =
    OptionMapper2.plain.asInstanceOf[OptionMapper2[CC, CC, BR, CC, B2, BR]]

  @inline implicit def getCCOptionMapper2TO[B1, B2: BaseTypedType, BR, CC](
                                                                            implicit ev: ValueClassLike[CC, B1]): OptionMapper2[B1, B2, BR, CC, Option[B2], Option[BR]] =
    OptionMapper2.option.asInstanceOf[OptionMapper2[B1, B2, BR, CC, Option[B2], Option[BR]]]

  @inline implicit def getCCOptionMapper2OT[B1, B2: BaseTypedType, BR, CC](
                                                                            implicit ev: ValueClassLike[CC, B1]): OptionMapper2[B1, B2, BR, Option[CC], B2, Option[BR]] =
    OptionMapper2.option.asInstanceOf[OptionMapper2[B1, B2, BR, Option[CC], B2, Option[BR]]]

  @inline implicit def getCCOptionMapper2OO[B1, B2: BaseTypedType, BR, CC](
                                                                            implicit ev: ValueClassLike[CC, B1]): OptionMapper2[B1, B2, BR, Option[CC], Option[B2], Option[BR]] =
    OptionMapper2.option.asInstanceOf[OptionMapper2[B1, B2, BR, Option[CC], Option[B2], Option[BR]]]
}

trait Kebs extends KebsColumnExtensionMethods {
  import pl.iterators.kebs.core.macros.CaseClass1ToValueClass._
  implicit def optionJdbcType[A]: GenericJdbcType[Option[A]] = new GenericJdbcType[Option[A]]("text", s => Some(s.asInstanceOf[A]), _.toString, java.sql.Types.OTHER)
  implicit def parametrizedJdbcType[VC, B](implicit cc: ValueClassLike[VC, B], ct: ClassTag[B]): JdbcType[B] =
    new GenericJdbcType[B]("text", s => s.asInstanceOf[B], _.toString, java.sql.Types.OTHER)

  private type MyMappedJdbcType[CC, B] = slick.jdbc.JdbcTypesComponent#MappedJdbcType[CC, B]

  implicit def valueColumnType[CC, B](implicit rep1: ValueClassLike[CC, B], bct: JdbcProfile#BaseColumnType[B], ct: ClassTag[CC], jp: JdbcProfile): MyMappedJdbcType[CC, B] =
    jp.MappedJdbcType.base[CC, B](rep1.unapply, rep1.apply).asInstanceOf[MyMappedJdbcType[CC, B]]

  implicit def valueTransitionColumnType[CC, B](implicit ico: InstanceConverter[CC, B], bct: JdbcProfile#BaseColumnType[B], ct: ClassTag[CC], jp: JdbcProfile): MyMappedJdbcType[CC, B] =
    jp.MappedJdbcType.base[CC, B](ico.encode, ico.decode).asInstanceOf[MyMappedJdbcType[CC, B]]

  implicit def listValueColumnType[CC, B](implicit iso: MyMappedJdbcType[CC, B], ct: ClassTag[CC], jp: JdbcProfile): MyMappedJdbcType[List[CC], List[B]] = {
    implicit val jt: JdbcType[List[B]] = new ListJdbcType[B]
    jp.MappedColumnType.base[List[CC], List[B]](_.map(iso.map), _.map(iso.comap)).asInstanceOf[MyMappedJdbcType[List[CC], List[B]]]
  }

  implicit def seqValueColumnType[CC, B](implicit iso: MyMappedJdbcType[CC, B], ct: ClassTag[CC], jp: JdbcProfile): MyMappedJdbcType[Seq[CC], List[B]] = {
    implicit val jt: JdbcType[List[B]] = new ListJdbcType[B]
    jp.MappedColumnType.base[Seq[CC], List[B]](_.map(iso.map).toList, _.map(iso.comap)).asInstanceOf[MyMappedJdbcType[Seq[CC], List[B]]]
  }

  implicit def mapValueColumnType[CC1, CC2, A, B](
                                                   implicit iso1: MyMappedJdbcType[CC1, A],
                                                   iso2: MyMappedJdbcType[CC2, B],
                                                   jp: JdbcProfile): MyMappedJdbcType[Map[CC1, CC2], Map[A, B]] = {
    implicit val jt: JdbcType[Map[A, B]] = new MapJdbcType[A, B]
    jp.MappedColumnType.base[Map[CC1, CC2], Map[A, B]](
      _.map { case (cc1, cc2) => (iso1.map(cc1), iso2.map(cc2)) },
      _.map { case (a, b) => (iso1.comap(a), iso2.comap(b)) }
    ).asInstanceOf[MyMappedJdbcType[Map[CC1, CC2], Map[A, B]]]
  }

  //  private class StringMapIsomorphism[A](fromStringMap: String => A) {
  //    def map(t: Map[String, A]): Map[String, String] =   t.map { case (str, a)     => (str, a.toString) }
  //    def comap(u: Map[String, String]): Map[String, A] = u.map { case (str1, str2) => (str1, fromStringMap(str2)) }
  //  }
  //
  //  implicit final val intMapValueColumnType: MyMappedJdbcType[Map[String, Int], Map[String, String]] =
  //    new StringMapIsomorphism[Int](_.toInt).asInstanceOf[MyMappedJdbcType[Map[String, Int], Map[String, String]]]
  //  implicit final val longMapValueColumnType: MyMappedJdbcType[Map[String, Long], Map[String, String]] =
  //    new StringMapIsomorphism[Long](_.toLong).asInstanceOf[MyMappedJdbcType[Map[String, Long], Map[String, String]]]
  //  implicit final val boolMapValueColumnType: MyMappedJdbcType[Map[String, Boolean], Map[String, String]] =
  //    new StringMapIsomorphism[Boolean](_.toBoolean).asInstanceOf[MyMappedJdbcType[Map[String, Boolean], Map[String, String]]]
  //
  //  private class StringValueMapIsomorphism[A](fromStringMap: String => A) {
  //    def map(t: Map[A, String]): Map[String, String] =   t.map { case (a, str)     => (a.toString, str) }
  //    def comap(u: Map[String, String]): Map[A, String] = u.map { case (str1, str2) => (fromStringMap(str1), str2) }
  //  }
  //  implicit final val intMapValueColumnType1: MyMappedJdbcType[Map[Int, String], Map[String, String]] =
  //    new StringValueMapIsomorphism[Int](_.toInt).asInstanceOf[MyMappedJdbcType[Map[Int, String], Map[String, String]]]
  //  implicit final val longMapValueColumnType1: MyMappedJdbcType[Map[Long, String], Map[String, String]] =
  //    new StringValueMapIsomorphism[Long](_.toLong).asInstanceOf[MyMappedJdbcType[Map[Long, String], Map[String, String]]]
  //  implicit final val boolMapValueColumnType1: MyMappedJdbcType[Map[Boolean, String], Map[String, String]] =
  //    new StringValueMapIsomorphism[Boolean](_.toBoolean).asInstanceOf[MyMappedJdbcType[Map[Boolean, String], Map[String, String]]]

  implicit def hstoreColumnType[CC1, CC2, A, B](
                                                 implicit iso1: MyMappedJdbcType[Map[CC1, CC2], Map[A, B]],
                                                 iso2: MyMappedJdbcType[Map[A, B], Map[String, String]],
                                                 jp: JdbcProfile): MyMappedJdbcType[Map[CC1, CC2], Map[String, String]] = {
    implicit val jt: JdbcType[Map[String, String]] = new MapJdbcType[String, String]
    jp.MappedColumnType.base[Map[CC1, CC2], Map[String, String]](
      iso1.map _ andThen iso2.map,
      iso2.comap _ andThen iso1.comap).asInstanceOf[MyMappedJdbcType[Map[CC1, CC2], Map[String, String]]]
  }

  def instancesIsoMapVal[Obj, Value, MapVal](comap1: String => Value, comap2: String => MapVal)(
    implicit ico: InstanceConverter[Obj, Value],
    jp: JdbcProfile,
    ct: ClassTag[Obj]) = {
    implicit val jt: JdbcType[Map[String, String]] = new MapJdbcType[String, String]
    jp.MappedColumnType.base[Map[Obj, MapVal], Map[String, String]](
      _.map { case (obj, mapval) => (ico.encode(obj).toString, mapval.toString) },
      _.map { case (value, str) => (ico.decode(comap1(value)), comap2(str)) }
    ).asInstanceOf[MyMappedJdbcType[Map[Obj, MapVal], Map[String, String]]]
  }

  def instancesIsoMapKey[Obj, Value, MapKey](comap1: String => Value, comap2: String => MapKey)(
    implicit ico: InstanceConverter[Obj, Value],
    jp: JdbcProfile) = {
    implicit val jt: JdbcType[Map[String, String]] = new MapJdbcType[String, String]
    jp.MappedColumnType.base[Map[MapKey, Obj], Map[String, String]](
      _.map { case (mapkey, obj) => (mapkey.toString, ico.encode(obj).toString) },
      _.map { case (str1, str2) => (comap2(str1), ico.decode(comap1(str2))) }
    ).asInstanceOf[MyMappedJdbcType[Map[MapKey, Obj], Map[String, String]]]
  }

  implicit def instancesIsoObj2Str[Obj](implicit ico: InstanceConverter[Obj, String], ct: ClassTag[Obj], jp: JdbcProfile): MyMappedJdbcType[Map[Obj, String], Map[String, String]] = {
    implicit val jt: JdbcType[Map[String, String]] = new MapJdbcType[String, String]
    instancesIsoMapVal[Obj, String, String](identity[String], identity[String])
  }

  implicit def instancesIsoObj2Str1[Obj](implicit ico: InstanceConverter[Obj, String], jp: JdbcProfile, ct: ClassTag[Obj]): MyMappedJdbcType[Map[String, Obj], Map[String, String]] = {
    implicit val jt: JdbcType[Map[String, String]] = new MapJdbcType[String, String]
    instancesIsoMapKey[Obj, String, String](identity[String], identity[String])
  }

  implicit def instancesIsoObj2Str2[Obj](implicit ico: InstanceConverter[Obj, String], jp: JdbcProfile, ct: ClassTag[Obj]): MyMappedJdbcType[Map[Obj, Int], Map[String, String]] = {
    implicit val jt: JdbcType[Map[String, String]] = new MapJdbcType[String, String]
    instancesIsoMapVal[Obj, String, Int](identity[String], _.toInt)
  }

  implicit def instancesIsoObj2Str3[Obj](implicit ico: InstanceConverter[Obj, String], jp: JdbcProfile, ct: ClassTag[Obj]): MyMappedJdbcType[Map[Int, Obj], Map[String, String]] = {
    implicit val jt: JdbcType[Map[String, String]] = new MapJdbcType[String, String]
    instancesIsoMapKey[Obj, String, Int](identity[String], _.toInt)
  }

  implicit def instancesIsoObj2Str4[Obj](implicit ico: InstanceConverter[Obj, String], jp: JdbcProfile, ct: ClassTag[Obj]): MyMappedJdbcType[Map[Obj, Long], Map[String, String]] = {
    implicit val jt: JdbcType[Map[String, String]] = new MapJdbcType[String, String]
    instancesIsoMapVal[Obj, String, Long](identity[String], _.toLong)
  }

  implicit def instancesIsoObj2Str5[Obj](implicit ico: InstanceConverter[Obj, String], jp: JdbcProfile, ct: ClassTag[Obj]): MyMappedJdbcType[Map[Long, Obj], Map[String, String]] = {
    implicit val jt: JdbcType[Map[String, String]] = new MapJdbcType[String, String]
    instancesIsoMapKey[Obj, String, Long](identity[String], _.toLong)
  }

  implicit def instancesIsoObj2Str6[Obj](implicit ico: InstanceConverter[Obj, String], jp: JdbcProfile, ct: ClassTag[Obj]): MyMappedJdbcType[Map[Obj, Boolean], Map[String, String]] = {
    implicit val jt: JdbcType[Map[String, String]] = new MapJdbcType[String, String]
    instancesIsoMapVal[Obj, String, Boolean](identity[String], _.toBoolean)
  }

  implicit def instancesIsoObj2Str7[Obj](implicit ico: InstanceConverter[Obj, String], jp: JdbcProfile, ct: ClassTag[Obj]): MyMappedJdbcType[Map[Boolean, Obj], Map[String, String]] = {
    implicit val jt: JdbcType[Map[String, String]] = new MapJdbcType[String, String]
    instancesIsoMapKey[Obj, String, Boolean](identity[String], _.toBoolean)
  }

  implicit def instancesIsoObj2Int[Obj](implicit ico: InstanceConverter[Obj, Int], jp: JdbcProfile, ct: ClassTag[Obj]): MyMappedJdbcType[Map[Obj, String], Map[String, String]] = {
    implicit val jt: JdbcType[Map[String, String]] = new MapJdbcType[String, String]
    instancesIsoMapVal[Obj, Int, String](_.toInt, identity[String])
  }

  implicit def instancesIsoObj2Int1[Obj](implicit ico: InstanceConverter[Obj, Int], jp: JdbcProfile, ct: ClassTag[Obj]): MyMappedJdbcType[Map[String, Obj], Map[String, String]] = {
    implicit val jt: JdbcType[Map[String, String]] = new MapJdbcType[String, String]
    instancesIsoMapKey[Obj, Int, String](_.toInt, identity[String])
  }

  implicit def instancesIsoObj2Int2[Obj](implicit ico: InstanceConverter[Obj, Int], jp: JdbcProfile, ct: ClassTag[Obj]): MyMappedJdbcType[Map[Obj, Int], Map[String, String]] = {
    implicit val jt: JdbcType[Map[String, String]] = new MapJdbcType[String, String]
    instancesIsoMapVal[Obj, Int, Int](_.toInt, _.toInt)
  }

  implicit def instancesIsoObj2Int3[Obj](implicit ico: InstanceConverter[Obj, Int], jp: JdbcProfile, ct: ClassTag[Obj]): MyMappedJdbcType[Map[Int, Obj], Map[String, String]] = {
    implicit val jt: JdbcType[Map[String, String]] = new MapJdbcType[String, String]
    instancesIsoMapKey[Obj, Int, Int](_.toInt, _.toInt)
  }

  implicit def instancesIsoObj2Int4[Obj](implicit ico: InstanceConverter[Obj, Int], jp: JdbcProfile, ct: ClassTag[Obj]): MyMappedJdbcType[Map[Obj, Long], Map[String, String]] = {
    implicit val jt: JdbcType[Map[String, String]] = new MapJdbcType[String, String]
    instancesIsoMapVal[Obj, Int, Long](_.toInt, _.toLong)
  }

  implicit def instancesIsoObj2Int5[Obj](implicit ico: InstanceConverter[Obj, Int], jp: JdbcProfile, ct: ClassTag[Obj]): MyMappedJdbcType[Map[Long, Obj], Map[String, String]] = {
    implicit val jt: JdbcType[Map[String, String]] = new MapJdbcType[String, String]
    instancesIsoMapKey[Obj, Int, Long](_.toInt, _.toLong)
  }

  implicit def instancesIsoObj2Int6[Obj](implicit ico: InstanceConverter[Obj, Int], jp: JdbcProfile, ct: ClassTag[Obj]): MyMappedJdbcType[Map[Obj, Boolean], Map[String, String]] = {
    implicit val jt: JdbcType[Map[String, String]] = new MapJdbcType[String, String]
    instancesIsoMapVal[Obj, Int, Boolean](_.toInt, _.toBoolean)
  }

  implicit def instancesIsoObj2Int7[Obj](implicit ico: InstanceConverter[Obj, Int], jp: JdbcProfile, ct: ClassTag[Obj]): MyMappedJdbcType[Map[Boolean, Obj], Map[String, String]] = {
    implicit val jt: JdbcType[Map[String, String]] = new MapJdbcType[String, String]
    instancesIsoMapKey[Obj, Int, Boolean](_.toInt, _.toBoolean)
  }

  implicit def instancesIsoObj2Long[Obj](implicit ico: InstanceConverter[Obj, Long], jp: JdbcProfile, ct: ClassTag[Obj]): MyMappedJdbcType[Map[Obj, String], Map[String, String]] = {
    implicit val jt: JdbcType[Map[String, String]] = new MapJdbcType[String, String]
    instancesIsoMapVal[Obj, Long, String](_.toLong, identity[String])
  }

  implicit def instancesIsoObj2Long1[Obj](implicit ico: InstanceConverter[Obj, Long], jp: JdbcProfile, ct: ClassTag[Obj]): MyMappedJdbcType[Map[String, Obj], Map[String, String]] = {
    implicit val jt: JdbcType[Map[String, String]] = new MapJdbcType[String, String]
    instancesIsoMapKey[Obj, Long, String](_.toLong, identity[String])
  }

  implicit def instancesIsoObj2Long2[Obj](implicit ico: InstanceConverter[Obj, Long], jp: JdbcProfile, ct: ClassTag[Obj]): MyMappedJdbcType[Map[Obj, Int], Map[String, String]] = {
    implicit val jt: JdbcType[Map[String, String]] = new MapJdbcType[String, String]
    instancesIsoMapVal[Obj, Long, Int](_.toLong, _.toInt)
  }

  implicit def instancesIsoObj2Long3[Obj](implicit ico: InstanceConverter[Obj, Long], jp: JdbcProfile, ct: ClassTag[Obj]): MyMappedJdbcType[Map[Int, Obj], Map[String, String]] = {
    implicit val jt: JdbcType[Map[String, String]] = new MapJdbcType[String, String]
    instancesIsoMapKey[Obj, Long, Int](_.toLong, _.toInt)
  }

  implicit def instancesIsoObj2Long4[Obj](implicit ico: InstanceConverter[Obj, Long], jp: JdbcProfile, ct: ClassTag[Obj]): MyMappedJdbcType[Map[Obj, Long], Map[String, String]] = {
    implicit val jt: JdbcType[Map[String, String]] = new MapJdbcType[String, String]
    instancesIsoMapVal[Obj, Long, Long](_.toLong, _.toLong)
  }

  implicit def instancesIsoObj2Long5[Obj](implicit ico: InstanceConverter[Obj, Long], jp: JdbcProfile, ct: ClassTag[Obj]): MyMappedJdbcType[Map[Long, Obj], Map[String, String]] = {
    implicit val jt: JdbcType[Map[String, String]] = new MapJdbcType[String, String]
    instancesIsoMapKey[Obj, Long, Long](_.toLong, _.toLong)
  }

  implicit def instancesIsoObj2Long6[Obj](implicit ico: InstanceConverter[Obj, Long], jp: JdbcProfile, ct: ClassTag[Obj]): MyMappedJdbcType[Map[Obj, Boolean], Map[String, String]] = {
    implicit val jt: JdbcType[Map[String, String]] = new MapJdbcType[String, String]
    instancesIsoMapVal[Obj, Long, Boolean](_.toLong, _.toBoolean)
  }

  implicit def instancesIsoObj2Long7[Obj](implicit ico: InstanceConverter[Obj, Long], jp: JdbcProfile, ct: ClassTag[Obj]): MyMappedJdbcType[Map[Boolean, Obj], Map[String, String]] = {
    implicit val jt: JdbcType[Map[String, String]] = new MapJdbcType[String, String]
    instancesIsoMapKey[Obj, Long, Boolean](_.toLong, _.toBoolean)
  }
}
