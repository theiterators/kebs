package pl.iterators.kebs.slick.enums

import enumeratum.values.{ValueEnum, ValueEnumEntry}
import pl.iterators.kebs.slick.Kebs
import pl.iterators.kebs.core.enums.{EnumLike, ValueEnumLike, ValueEnumLikeEntry}
import slick.jdbc.{JdbcProfile, JdbcType}

import scala.reflect.ClassTag

trait SlickEnum extends Kebs {

  import slick.jdbc.PostgresProfile.api._

  def enumIsomorphism[E](
                                       `enum`: EnumLike[E])(
                                       implicit ct: ClassTag[E],
                                       jp: JdbcProfile): slick.jdbc.JdbcTypesComponent#MappedJdbcType[E, String] = {
    jp.MappedColumnType.base[E, String](_.toString, `enum`.withName).asInstanceOf[slick.jdbc.JdbcTypesComponent#MappedJdbcType[E, String]]
  }

  def uppercaseEnumIsomorphism[E](
                                                `enum`: EnumLike[E])(
                                                implicit ct: ClassTag[E],
                                                jp: JdbcProfile): slick.jdbc.JdbcTypesComponent#MappedJdbcType[E, String] = jp.MappedColumnType.base[E, String](_.toString.toUpperCase, `enum`.withNameUppercaseOnly).asInstanceOf[slick.jdbc.JdbcTypesComponent#MappedJdbcType[E, String]]

  def lowercaseEnumIsomorphism[E](
                                                `enum`: EnumLike[E])(
                                                implicit ct: ClassTag[E],
                                                jp: JdbcProfile): slick.jdbc.JdbcTypesComponent#MappedJdbcType[E, String] = jp.MappedColumnType.base[E, String](_.toString.toLowerCase, `enum`.withNameLowercaseOnly).asInstanceOf[slick.jdbc.JdbcTypesComponent#MappedJdbcType[E, String]]

  implicit def enumListColumnType[E](implicit iso: slick.jdbc.JdbcTypesComponent#MappedJdbcType[E, String], jp: JdbcProfile): slick.jdbc.JdbcTypesComponent#MappedJdbcType[List[E], List[String]] = {
    implicit val jt: JdbcType[List[String]] = new ListJdbcType[List[String]].asInstanceOf[JdbcType[List[String]]]
    jp.MappedColumnType.base[List[E], List[String]](_.map(iso.map), _.map(iso.comap)).asInstanceOf[slick.jdbc.JdbcTypesComponent#MappedJdbcType[List[E], List[String]]]
  }

  implicit def enumSeqColumnType[E](implicit iso: slick.jdbc.JdbcTypesComponent#MappedJdbcType[E, String], jp: JdbcProfile): slick.jdbc.JdbcTypesComponent#MappedJdbcType[Seq[E], List[String]] = {
    implicit val jt: JdbcType[List[String]] = new ListJdbcType[List[String]].asInstanceOf[JdbcType[List[String]]]
    jp.MappedColumnType.base[Seq[E], List[String]](_.map(iso.map).toList, _.map(iso.comap)).asInstanceOf[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Seq[E], List[String]]]
  }
}

trait SlickValueEnum {
  def valueEnumIsomorphism[V, E <: ValueEnumLikeEntry[V]](`enum`: ValueEnumLike[V, E])(implicit bct: JdbcType[V], ct: ClassTag[E], jp: JdbcProfile): slick.jdbc.JdbcTypesComponent#MappedJdbcType[E, V] =
    jp.MappedColumnType.base[E, V](_.value, `enum`.withValue).asInstanceOf[slick.jdbc.JdbcTypesComponent#MappedJdbcType[E, V]]
}

trait KebsEnums extends SlickEnum with SlickValueEnum {
  implicit def enumValueColumn[E](implicit ev: EnumLike[E], ct: ClassTag[E], jt: slick.jdbc.JdbcType[String], jp: JdbcProfile): slick.jdbc.JdbcTypesComponent#MappedJdbcType[E, String] = enumIsomorphism(ev)

  implicit def valueEnumColumn[V, E <: ValueEnumLikeEntry[V]](implicit ev: ValueEnumLike[V, E], bct: JdbcProfile#BaseColumnType[V], ct: ClassTag[E], jp: JdbcProfile): slick.jdbc.JdbcTypesComponent#MappedJdbcType[E, V] =
    valueEnumIsomorphism(ev)

  trait Uppercase extends SlickEnum {
    implicit def enumValueColumn[E](implicit ev: EnumLike[E], jt: slick.jdbc.JdbcType[String], ct: ClassTag[E], jp: JdbcProfile): slick.jdbc.JdbcTypesComponent#MappedJdbcType[E, String] = uppercaseEnumIsomorphism(ev)
  }

  trait Lowercase extends SlickEnum {
    implicit def enumValueColumn[E](implicit ev: EnumLike[E], jt: slick.jdbc.JdbcType[String], ct: ClassTag[E], jp: JdbcProfile): slick.jdbc.JdbcTypesComponent#MappedJdbcType[E, String] = lowercaseEnumIsomorphism(ev)
  }
}

object KebsEnums extends KebsEnums
