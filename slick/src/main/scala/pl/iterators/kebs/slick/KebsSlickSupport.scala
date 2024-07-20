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

trait KebsSlickSupport { this: JdbcProfile =>
  trait ToFromStringForHstore[T] {
    def to(value: T): String
    def from(value: String): T
  }

  trait KebsBasicImplicits extends KebsColumnExtensionMethods {
    implicit def hstoreColumnType[A, B](
                                         implicit k: ToFromStringForHstore[A],
                                         v: ToFromStringForHstore[B],
                                         bct: BaseColumnType[Map[String, String]]
                                                 ): BaseColumnType[Map[A, B]] =
        MappedColumnType.base[Map[A, B], Map[String, String]](
            _.map { case (a, b) => (k.to(a), v.to(b)) },
            _.map { case (a, b) => (k.from(a), v.from(b)) }
            )

    implicit val intToFromStringForHstore: ToFromStringForHstore[Int] = new ToFromStringForHstore[Int] {
      override def to(value: Int): String = value.toString
      override def from(value: String): Int = value.toInt
    }

    implicit val longToFromStringForHstore: ToFromStringForHstore[Long] = new ToFromStringForHstore[Long] {
      override def to(value: Long): String = value.toString
      override def from(value: String): Long = value.toLong
    }

    implicit val booleanToFromStringForHstore: ToFromStringForHstore[Boolean] = new ToFromStringForHstore[Boolean] {
      override def to(value: Boolean): String = value.toString
      override def from(value: String): Boolean = value.toBoolean
    }

    implicit val stringToFromStringForHstore: ToFromStringForHstore[String] = new ToFromStringForHstore[String] {
      override def to(value: String): String = value
      override def from(value: String): String = value
    }

    implicit val doubleToFromStringForHstore: ToFromStringForHstore[Double] = new ToFromStringForHstore[Double] {
      override def to(value: Double): String = value.toString
      override def from(value: String): Double = value.toDouble
    }

    implicit val floatToFromStringForHstore: ToFromStringForHstore[Float] = new ToFromStringForHstore[Float] {
      override def to(value: Float): String = value.toString
      override def from(value: String): Float = value.toFloat
    }

    implicit val shortToFromStringForHstore: ToFromStringForHstore[Short] = new ToFromStringForHstore[Short] {
      override def to(value: Short): String = value.toString
      override def from(value: String): Short = value.toShort
    }

    implicit val byteToFromStringForHstore: ToFromStringForHstore[Byte] = new ToFromStringForHstore[Byte] {
      override def to(value: Byte): String = value.toString
      override def from(value: String): Byte = value.toByte
    }

    implicit val charToFromStringForHstore: ToFromStringForHstore[Char] = new ToFromStringForHstore[Char] {
      override def to(value: Char): String = value.toString
      override def from(value: String): Char = value.head
    }

    implicit val bigDecimalToFromStringForHstore: ToFromStringForHstore[BigDecimal] = new ToFromStringForHstore[BigDecimal] {
      override def to(value: BigDecimal): String = value.toString
      override def from(value: String): BigDecimal = BigDecimal(value)
    }

    implicit val bigIntToFromStringForHstore: ToFromStringForHstore[BigInt] = new ToFromStringForHstore[BigInt] {
      override def to(value: BigInt): String = value.toString
      override def from(value: String): BigInt = BigInt(value)
    }
  }

  trait KebsValueClassLikeImplicits {
    implicit def valueClassLikeColumnType[CC, B](implicit rep1: ValueClassLike[CC, B], bct: BaseColumnType[B], cls: ClassTag[CC]): BaseColumnType[CC] =
      MappedColumnType.base[CC, B](rep1.unapply, rep1.apply)

    implicit def listValueColumnType[CC, B](implicit rep1: ValueClassLike[CC, B], bct: BaseColumnType[List[B]], cls: ClassTag[CC]): BaseColumnType[List[CC]] =
      MappedColumnType.base[List[CC], List[B]](_.map(rep1.unapply), _.map(rep1.apply))

    implicit def valueClassLikeToFromStringForHstore[CC, B](implicit rep: ValueClassLike[CC, B], toFromStringForHstore: ToFromStringForHstore[B]): ToFromStringForHstore[CC] =
      new ToFromStringForHstore[CC] {
        override def to(value: CC): String = toFromStringForHstore.to(rep.unapply(value))
        override def from(value: String): CC = rep.apply(toFromStringForHstore.from(value))
      }
  }

  trait KebsInstanceConverterImplicits {
    implicit def instanceConverterColumnType[CC, B](implicit ico: InstanceConverter[CC, B], bct: BaseColumnType[B], cls: ClassTag[CC]): BaseColumnType[CC] =
      MappedColumnType.base[CC, B](ico.encode, ico.decode)

    implicit def listInstanceConverterColumnType[CC, B](implicit ico: InstanceConverter[CC, B], bct: BaseColumnType[List[B]], cls: ClassTag[CC]): BaseColumnType[List[CC]] =
      MappedColumnType.base[List[CC], List[B]](_.map(ico.encode), _.map(ico.decode))

    implicit def instanceConverterToFromStringForHstore[CC, B](implicit ico: InstanceConverter[CC, B], toFromStringForHstore: ToFromStringForHstore[B]): ToFromStringForHstore[CC] =
      new ToFromStringForHstore[CC] {
        override def to(value: CC): String = toFromStringForHstore.to(ico.encode(value))
        override def from(value: String): CC = ico.decode(toFromStringForHstore.from(value))
      }
  }

  protected trait SlickEnum {
    def enumColumn[E](`enum`: EnumLike[E])(implicit bct: BaseColumnType[String], cls: ClassTag[E]): BaseColumnType[E] = MappedColumnType.base[E, String](_.toString, `enum`.withName)

    def uppercaseEnumColumn[E](`enum`: EnumLike[E])(implicit bct: BaseColumnType[String], cls: ClassTag[E]): BaseColumnType[E] =
      MappedColumnType.base[E, String](_.toString.toUpperCase, `enum`.withNameUppercaseOnly)

    def lowercaseEnumColumn[E](`enum`: EnumLike[E])(implicit bct: BaseColumnType[String], cls: ClassTag[E]): BaseColumnType[E] =
      MappedColumnType.base[E, String](_.toString.toLowerCase, `enum`.withNameLowercaseOnly)
  }

  protected trait SlickValueEnum {
    def valueEnumColumnType[V, E <: ValueEnumLikeEntry[V]](`enum`: ValueEnumLike[V, E])(implicit bct: BaseColumnType[V], cls: ClassTag[E]): BaseColumnType[E] =
      MappedColumnType.base[E, V](_.value, `enum`.withValue)
  }

  trait EnumImplicits extends SlickValueEnum with SlickEnum {
    implicit def enumValueColumn[E](implicit ev: EnumLike[E], bct: BaseColumnType[String], cls: ClassTag[E]): BaseColumnType[E] = enumColumn(ev)

    implicit def valueEnumColumn[V, E <: ValueEnumLikeEntry[V]](implicit ev: ValueEnumLike[V, E], bct: BaseColumnType[V], cls: ClassTag[E]): BaseColumnType[E] =
      valueEnumColumnType(ev)

    implicit def enumListColumn[E](implicit ev: EnumLike[E], bct: BaseColumnType[List[String]]): BaseColumnType[List[E]] = {
      MappedColumnType.base[List[E], List[String]](_.map(_.toString), _.map(ev.withName))
    }

    implicit def enumToFromStringForHstore[E](implicit ev: EnumLike[E]): ToFromStringForHstore[E] = new ToFromStringForHstore[E] {
      override def to(value: E): String = value.toString
      override def from(value: String): E = ev.withName(value)
    }

    implicit def valueEnumToFromStringForHstore[V, E <: ValueEnumLikeEntry[V]](implicit ev: ValueEnumLike[V, E], tfs: ToFromStringForHstore[V]): ToFromStringForHstore[E] = new ToFromStringForHstore[E] {
      override def to(value: E): String = tfs.to(value.value)
      override def from(value: String): E = ev.withValue(tfs.from(value))
    }
  }

  trait LowercaseEnumImplicits extends SlickValueEnum with SlickEnum {
    implicit def enumValueColumn[E](implicit ev: EnumLike[E], bct: BaseColumnType[String], cls: ClassTag[E]): BaseColumnType[E] = lowercaseEnumColumn(ev)

    implicit def enumListColumn[E](implicit ev: EnumLike[E], bct: BaseColumnType[List[String]]): BaseColumnType[List[E]] = {
      MappedColumnType.base[List[E], List[String]](_.map(_.toString.toLowerCase), _.map(ev.withNameLowercaseOnly))
    }

    implicit def toFromStringForHstoreEnum[E](implicit ev: EnumLike[E]): ToFromStringForHstore[E] = new ToFromStringForHstore[E] {
      override def to(value: E): String = value.toString.toLowerCase
      override def from(value: String): E = ev.withNameLowercaseOnly(value)
    }
  }

  trait UppercaseEnumImplicits extends SlickValueEnum with SlickEnum {
    implicit def enumValueColumn[E](implicit ev: EnumLike[E], bct: BaseColumnType[String], cls: ClassTag[E]): BaseColumnType[E] = uppercaseEnumColumn(ev)

    implicit def enumListColumn[E](implicit ev: EnumLike[E], bct: BaseColumnType[List[String]]): BaseColumnType[List[E]] = {
      MappedColumnType.base[List[E], List[String]](_.map(_.toString.toUpperCase), _.map(ev.withNameUppercaseOnly))
    }

    implicit def toFromStringForHstoreEnum[E](implicit ev: EnumLike[E]): ToFromStringForHstore[E] = new ToFromStringForHstore[E] {
      override def to(value: E): String = value.toString.toUpperCase
      override def from(value: String): E = ev.withNameUppercaseOnly(value)
    }
  }
}
