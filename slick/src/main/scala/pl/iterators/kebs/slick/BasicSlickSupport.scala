package pl.iterators.kebs.slick

import pl.iterators.kebs.core.enums.{EnumLike, ValueEnumLike, ValueEnumLikeEntry}
import pl.iterators.kebs.core.instances.InstanceConverter
import pl.iterators.kebs.core.macros.ValueClassLike
import pl.iterators.kebs.slick.hstore.KebsHStoreColumnExtensionMethods
import slick.ast.{BaseTypedType, NumericTypedType}
import slick.jdbc.{JdbcProfile, JdbcType}
import slick.lifted._

import scala.language.implicitConversions
import scala.reflect.ClassTag

trait KebsColumnExtensionMethods  {
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

trait BasicSlickSupport { this: JdbcProfile =>
  trait BasicSlickImplicits extends KebsColumnExtensionMethods {
    implicit final def intMapValueColumnType(implicit bct: BaseColumnType[Map[String, String]]): BaseColumnType[Map[Int, String]] = MappedColumnType.base[Map[Int, String], Map[String, String]](
      _.map { case (int, str) => (int.toString, str) },
      _.map { case (str1, str2) => (str1.toInt, str2) }
    )

    implicit final def longMapValueColumnType(implicit bct: BaseColumnType[Map[String, String]]): BaseColumnType[Map[Long, String]] = MappedColumnType.base[Map[Long, String], Map[String, String]](
      _.map { case (long, str) => (long.toString, str) },
      _.map { case (str1, str2) => (str1.toLong, str2) }
    )

    implicit final def boolMapValueColumnType(implicit bct: BaseColumnType[Map[String, String]]): BaseColumnType[Map[Boolean, String]] = MappedColumnType.base[Map[Boolean, String], Map[String, String]](
      _.map { case (bool, str) => (bool.toString, str) },
      _.map { case (str1, str2) => (str1.toBoolean, str2) }
    )

    implicit final def intMapValueColumnType1(implicit bct: BaseColumnType[Map[String, String]]): BaseColumnType[Map[String, Int]] = MappedColumnType.base[Map[String, Int], Map[String, String]](
      _.map { case (str, int) => (str, int.toString) },
      _.map { case (str, str1) => (str, str1.toInt) }
    )

    implicit final def longMapValueColumnType1(implicit bct: BaseColumnType[Map[String, String]]): BaseColumnType[Map[String, Long]] = MappedColumnType.base[Map[String, Long], Map[String, String]](
      _.map { case (str, long) => (str, long.toString) },
      _.map { case (str, str1) => (str, str1.toLong) }
    )

    implicit final def boolMapValueColumnType1(implicit bct: BaseColumnType[Map[String, String]]): BaseColumnType[Map[String, Boolean]] = MappedColumnType.base[Map[String, Boolean], Map[String, String]](
      _.map { case (str, bool) => (str, bool.toString) },
      _.map { case (str, str1) => (str, str1.toBoolean) }
    )
  }

  trait ValueClassLikeImplicits {
    implicit def valueColumnType[CC, B](implicit rep1: ValueClassLike[CC, B], bct: BaseColumnType[B], cls: ClassTag[CC]): BaseColumnType[CC] =
      MappedColumnType.base[CC, B](rep1.unapply, rep1.apply)

    implicit def listValueColumnType[CC, B](implicit rep1: ValueClassLike[CC, B], bct: BaseColumnType[List[B]], cls: ClassTag[CC]): BaseColumnType[List[CC]] =
      MappedColumnType.base[List[CC], List[B]](_.map(rep1.unapply), _.map(rep1.apply))

    implicit def hstoreColumnType[CC1, CC2, A, B](
                                                   implicit rep1: ValueClassLike[CC1, A],
                                                   rep2: ValueClassLike[CC2, B],
                                                   bct: BaseColumnType[Map[A, B]]
                                                 ): BaseColumnType[Map[CC1, CC2]] =
      MappedColumnType.base[Map[CC1, CC2], Map[A, B]](_.map { case (cc1, cc2) => (rep1.unapply(cc1), rep2.unapply(cc2)) },
        _.map { case (a, b) => (rep1.apply(a), rep2.apply(b)) })

    implicit def hstoreColumnType1[CC1, A, B](
                                                   implicit rep1: ValueClassLike[CC1, A],
                                                   rep2: EnumLike[B],
                                                   bct: BaseColumnType[Map[A, String]]
                                                 ): BaseColumnType[Map[CC1, B]] =
        MappedColumnType.base[Map[CC1, B], Map[A, String]](_.map { case (cc1, b) => (rep1.unapply(cc1), b.toString) }, // TODO: casing
            _.map { case (a, str) => (rep1.apply(a), rep2.withName(str)) })

    implicit def hstoreColumnType2[CC2, A, B](
                                                   implicit rep1: EnumLike[A],
                                                   rep2: ValueClassLike[CC2, B],
                                                   bct: BaseColumnType[Map[String, B]]
                                                 ): BaseColumnType[Map[A, CC2]] =
        MappedColumnType.base[Map[A, CC2], Map[String, B]](_.map { case (a, cc2) => (a.toString, rep2.unapply(cc2)) },
            _.map { case (str, cc2) => (rep1.withName(str), rep2.apply(cc2)) })

    implicit def hstoreColumnType3[CC1, A, B](
                                               implicit rep1: ValueClassLike[CC1, A],
                                               rep2: EnumLike[B],
                                               bct: BaseColumnType[Map[A, String]]
                                             ): BaseColumnType[Map[CC1, B]] =
      MappedColumnType.base[Map[CC1, B], Map[A, String]](_.map { case (cc1, b) => (rep1.unapply(cc1), b.toString) }, // TODO: casing
        _.map { case (a, str) => (rep1.apply(a), rep2.withName(str)) })
  }

  trait InstanceConverterImplicits {
    implicit def valueTransitionColumnType[CC, B](implicit ico: InstanceConverter[CC, B], bct: BaseColumnType[B], cls: ClassTag[CC]): BaseColumnType[CC] =
      MappedColumnType.base[CC, B](ico.encode, ico.decode)

    implicit def listTransitionColumnType[CC, B](implicit ico: InstanceConverter[CC, B], bct: BaseColumnType[List[B]], cls: ClassTag[CC]): BaseColumnType[List[CC]] =
      MappedColumnType.base[List[CC], List[B]](_.map(ico.encode), _.map(ico.decode))

    implicit def seqTransitionColumnType[CC, B](implicit ico: InstanceConverter[CC, B], bct: BaseColumnType[Seq[B]], cls: ClassTag[CC]): BaseColumnType[Seq[CC]] = {
      MappedColumnType.base[Seq[CC], Seq[B]](_.map(ico.encode), _.map(ico.decode))
    }

    def instancesIsoMapVal[Obj, Value, MapVal](comap1: String => Value, comap2: String => MapVal)(
      implicit ico: InstanceConverter[Obj, Value], bct: BaseColumnType[Map[String, String]]): BaseColumnType[Map[Obj, MapVal]] =
      MappedColumnType.base[Map[Obj, MapVal], Map[String, String]](
        _.map { case (obj, mapval) => (ico.encode(obj).toString, mapval.toString) },
        _.map { case (str1, str2) => (ico.decode(comap1(str1)), comap2(str2)) }
      )

    def instancesIsoMapKey[Obj, Value, MapKey](comap1: String => Value, comap2: String => MapKey)(
      implicit ico: InstanceConverter[Obj, Value], bct: BaseColumnType[Map[String, String]]): BaseColumnType[Map[MapKey, Obj]] =
      MappedColumnType.base[Map[MapKey, Obj], Map[String, String]](
        _.map { case (mapkey, obj) => (mapkey.toString, ico.encode(obj).toString) },
        _.map { case (str1, str2) => (comap2(str1), ico.decode(comap1(str2))) }
      )

    implicit def instancesIsoObj2Str[Obj](implicit ico: InstanceConverter[Obj, String], bct: BaseColumnType[Map[String, String]]): BaseColumnType[Map[Obj, String]] =
      instancesIsoMapVal[Obj, String, String](identity[String], identity[String])

    implicit def instancesIsoObj2Str1[Obj](
                                            implicit ico: InstanceConverter[Obj, String], bct: BaseColumnType[Map[String, String]]): BaseColumnType[Map[String, Obj]] =
      instancesIsoMapKey[Obj, String, String](identity[String], identity[String])

    implicit def instancesIsoObj2Str2[Obj](implicit ico: InstanceConverter[Obj, String], bct: BaseColumnType[Map[String, String]]): BaseColumnType[Map[Obj, Int]] =
      instancesIsoMapVal[Obj, String, Int](identity[String], _.toInt)

    implicit def instancesIsoObj2Str3[Obj](implicit ico: InstanceConverter[Obj, String], bct: BaseColumnType[Map[String, String]]): BaseColumnType[Map[Int, Obj]] =
      instancesIsoMapKey[Obj, String, Int](identity[String], _.toInt)

    implicit def instancesIsoObj2Str4[Obj](implicit ico: InstanceConverter[Obj, String], bct: BaseColumnType[Map[String, String]]): BaseColumnType[Map[Obj, Long]] =
      instancesIsoMapVal[Obj, String, Long](identity[String], _.toLong)

    implicit def instancesIsoObj2Str5[Obj](implicit ico: InstanceConverter[Obj, String], bct: BaseColumnType[Map[String, String]]): BaseColumnType[Map[Long, Obj]] =
      instancesIsoMapKey[Obj, String, Long](identity[String], _.toLong)

    implicit def instancesIsoObj2Str6[Obj](
                                            implicit ico: InstanceConverter[Obj, String], bct: BaseColumnType[Map[String, String]]): BaseColumnType[Map[Obj, Boolean]] =
      instancesIsoMapVal[Obj, String, Boolean](identity[String], _.toBoolean)

    implicit def instancesIsoObj2Str7[Obj](
                                            implicit ico: InstanceConverter[Obj, String], bct: BaseColumnType[Map[String, String]]): BaseColumnType[Map[Boolean, Obj]] =
      instancesIsoMapKey[Obj, String, Boolean](identity[String], _.toBoolean)

    implicit def instancesIsoObj2Int[Obj](implicit ico: InstanceConverter[Obj, Int], bct: BaseColumnType[Map[String, String]]): BaseColumnType[Map[Obj, String]] =
      instancesIsoMapVal[Obj, Int, String](_.toInt, identity[String])

    implicit def instancesIsoObj2Int1[Obj](implicit ico: InstanceConverter[Obj, Int], bct: BaseColumnType[Map[String, String]]): BaseColumnType[Map[String, Obj]] =
      instancesIsoMapKey[Obj, Int, String](_.toInt, identity[String])

    implicit def instancesIsoObj2Int2[Obj](implicit ico: InstanceConverter[Obj, Int], bct: BaseColumnType[Map[String, String]]): BaseColumnType[Map[Obj, Int]] =
      instancesIsoMapVal[Obj, Int, Int](_.toInt, _.toInt)

    implicit def instancesIsoObj2Int3[Obj](implicit ico: InstanceConverter[Obj, Int], bct: BaseColumnType[Map[String, String]]): BaseColumnType[Map[Int, Obj]] =
      instancesIsoMapKey[Obj, Int, Int](_.toInt, _.toInt)

    implicit def instancesIsoObj2Int4[Obj](implicit ico: InstanceConverter[Obj, Int], bct: BaseColumnType[Map[String, String]]): BaseColumnType[Map[Obj, Long]] =
      instancesIsoMapVal[Obj, Int, Long](_.toInt, _.toLong)

    implicit def instancesIsoObj2Int5[Obj](implicit ico: InstanceConverter[Obj, Int], bct: BaseColumnType[Map[String, String]]): BaseColumnType[Map[Long, Obj]] =
      instancesIsoMapKey[Obj, Int, Long](_.toInt, _.toLong)

    implicit def instancesIsoObj2Int6[Obj](implicit ico: InstanceConverter[Obj, Int], bct: BaseColumnType[Map[String, String]]): BaseColumnType[Map[Obj, Boolean]] =
      instancesIsoMapVal[Obj, Int, Boolean](_.toInt, _.toBoolean)

    implicit def instancesIsoObj2Int7[Obj](implicit ico: InstanceConverter[Obj, Int], bct: BaseColumnType[Map[String, String]]): BaseColumnType[Map[Boolean, Obj]] =
      instancesIsoMapKey[Obj, Int, Boolean](_.toInt, _.toBoolean)

    implicit def instancesIsoObj2Long[Obj](implicit ico: InstanceConverter[Obj, Long], bct: BaseColumnType[Map[String, String]]): BaseColumnType[Map[Obj, String]] =
      instancesIsoMapVal[Obj, Long, String](_.toLong, identity[String])

    implicit def instancesIsoObj2Long1[Obj](implicit ico: InstanceConverter[Obj, Long], bct: BaseColumnType[Map[String, String]]): BaseColumnType[Map[String, Obj]] =
      instancesIsoMapKey[Obj, Long, String](_.toLong, identity[String])

    implicit def instancesIsoObj2Long2[Obj](implicit ico: InstanceConverter[Obj, Long], bct: BaseColumnType[Map[String, String]]): BaseColumnType[Map[Obj, Int]] =
      instancesIsoMapVal[Obj, Long, Int](_.toLong, _.toInt)

    implicit def instancesIsoObj2Long3[Obj](implicit ico: InstanceConverter[Obj, Long], bct: BaseColumnType[Map[String, String]]): BaseColumnType[Map[Int, Obj]] =
      instancesIsoMapKey[Obj, Long, Int](_.toLong, _.toInt)

    implicit def instancesIsoObj2Long4[Obj](implicit ico: InstanceConverter[Obj, Long], bct: BaseColumnType[Map[String, String]]): BaseColumnType[Map[Obj, Long]] =
      instancesIsoMapVal[Obj, Long, Long](_.toLong, _.toLong)

    implicit def instancesIsoObj2Long5[Obj](implicit ico: InstanceConverter[Obj, Long], bct: BaseColumnType[Map[String, String]]): BaseColumnType[Map[Long, Obj]] =
      instancesIsoMapKey[Obj, Long, Long](_.toLong, _.toLong)

    implicit def instancesIsoObj2Long6[Obj](
                                             implicit ico: InstanceConverter[Obj, Long], bct: BaseColumnType[Map[String, String]]): BaseColumnType[Map[Obj, Boolean]] =
      instancesIsoMapVal[Obj, Long, Boolean](_.toLong, _.toBoolean)

    implicit def instancesIsoObj2Long7[Obj](
                                             implicit ico: InstanceConverter[Obj, Long], bct: BaseColumnType[Map[String, String]]): BaseColumnType[Map[Boolean, Obj]] =
      instancesIsoMapKey[Obj, Long, Boolean](_.toLong, _.toBoolean)
  }

  protected trait SlickEnum {
    def enumIsomorphism[E](`enum`: EnumLike[E])(implicit bct: BaseColumnType[String], cls: ClassTag[E]): BaseColumnType[E] = MappedColumnType.base[E, String](_.toString, `enum`.withName)

    def uppercaseEnumIsomorphism[E](`enum`: EnumLike[E])(implicit bct: BaseColumnType[String], cls: ClassTag[E]): BaseColumnType[E] =
      MappedColumnType.base[E, String](_.toString.toUpperCase, `enum`.withNameUppercaseOnly)

    def lowercaseEnumIsomorphism[E](`enum`: EnumLike[E])(implicit bct: BaseColumnType[String], cls: ClassTag[E]): BaseColumnType[E] =
      MappedColumnType.base[E, String](_.toString.toLowerCase, `enum`.withNameLowercaseOnly)
  }

  protected trait SlickValueEnum {
    def valueEnumIsomorphism[V, E <: ValueEnumLikeEntry[V]](`enum`: ValueEnumLike[V, E])(implicit bct: BaseColumnType[V], cls: ClassTag[E]): BaseColumnType[E] =
      MappedColumnType.base[E, V](_.value, `enum`.withValue)
  }

  trait EnumImplicits extends SlickValueEnum with SlickEnum {
    implicit def enumValueColumn[E](implicit ev: EnumLike[E], bct: BaseColumnType[String], cls: ClassTag[E]): BaseColumnType[E] = enumIsomorphism(ev)

    implicit def valueEnumColumn[V, E <: ValueEnumLikeEntry[V]](implicit ev: ValueEnumLike[V, E], bct: BaseColumnType[V], cls: ClassTag[E]): BaseColumnType[E] =
      valueEnumIsomorphism(ev)

    implicit def enumListColumn[E](implicit ev: EnumLike[E], bct: BaseColumnType[List[String]]): BaseColumnType[List[E]] = {
      MappedColumnType.base[List[E], List[String]](_.map(_.toString), _.map(ev.withName))
    }
  }

  trait LowercaseEnumImplicits extends SlickValueEnum with SlickEnum {
    implicit def enumValueColumn[E](implicit ev: EnumLike[E], bct: BaseColumnType[String], cls: ClassTag[E]): BaseColumnType[E] = lowercaseEnumIsomorphism(ev)

    implicit def enumListColumn[E](implicit ev: EnumLike[E], bct: BaseColumnType[List[String]]): BaseColumnType[List[E]] = {
      MappedColumnType.base[List[E], List[String]](_.map(_.toString.toLowerCase), _.map(ev.withNameLowercaseOnly))
    }
  }

  trait UppercaseEnumImplicits extends SlickValueEnum with SlickEnum {
    implicit def enumValueColumn[E](implicit ev: EnumLike[E], bct: BaseColumnType[String], cls: ClassTag[E]): BaseColumnType[E] = uppercaseEnumIsomorphism(ev)

    implicit def enumListColumn[E](implicit ev: EnumLike[E], bct: BaseColumnType[List[String]]): BaseColumnType[List[E]] = {
      MappedColumnType.base[List[E], List[String]](_.map(_.toString.toUpperCase), _.map(ev.withNameUppercaseOnly))
    }
  }
}
