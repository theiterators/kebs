package pl.iterators.kebs.doobie

import cats.Show
import org.tpolecat.typename.TypeName
import org.typelevel.doobie.Meta
import pl.iterators.kebs.core.instances.InstanceConverter
import pl.iterators.kebs.core.macros.ValueClassLike

import scala.reflect.ClassTag

// a value that is rejected is doobie's InvalidValue, with the reason
trait KebsDoobie {
  implicit def valueClassLikeMeta[A, M](implicit vcLike: ValueClassLike[A, M], m: Meta[M], name: TypeName[A]): Meta[A] = {
    implicit val show: Show[M] = Show.fromToString
    m.tiemap(vcLike.from)(vcLike.unapply)
  }

  implicit def valueClassLikeArrayMeta[A, M](implicit
      vcLike: ValueClassLike[A, M],
      m: Meta[Array[M]],
      cta: ClassTag[A],
      ctm: ClassTag[M],
      name: TypeName[Array[A]]
  ): Meta[Array[A]] = {
    implicit val show: Show[Array[M]] = Show.fromToString
    m.tiemap(KebsDoobie.each[M, A](vcLike.from))(_.map(vcLike.unapply))
  }

  implicit def valueClassLikeOptionArrayMeta[A, M](implicit
      vcLike: ValueClassLike[A, M],
      m: Meta[Array[Option[M]]],
      name: TypeName[Array[Option[A]]]
  ): Meta[Array[Option[A]]] = {
    implicit val show: Show[Array[Option[M]]] = Show.fromToString
    m.tiemap(KebsDoobie.each[Option[M], Option[A]](KebsDoobie.option(vcLike.from)))(_.map(_.map(vcLike.unapply)))
  }

  implicit def instanceConverterMeta[A, M](implicit instanceConverter: InstanceConverter[A, M], m: Meta[M], name: TypeName[A]): Meta[A] = {
    implicit val show: Show[M] = Show.fromToString
    m.tiemap(instanceConverter.decodeEither)(instanceConverter.encode)
  }

  implicit def instanceConverterArrayMeta[A, M](implicit
      instanceConverter: InstanceConverter[A, M],
      m: Meta[Array[M]],
      cta: ClassTag[A],
      ctm: ClassTag[M],
      name: TypeName[Array[A]]
  ): Meta[Array[A]] = {
    implicit val show: Show[Array[M]] = Show.fromToString
    m.tiemap(KebsDoobie.each[M, A](instanceConverter.decodeEither))(_.map(instanceConverter.encode))
  }

  implicit def instanceConverterOptionArrayMeta[A, M](implicit
      instanceConverter: InstanceConverter[A, M],
      m: Meta[Array[Option[M]]],
      name: TypeName[Array[Option[A]]]
  ): Meta[Array[Option[A]]] = {
    implicit val show: Show[Array[Option[M]]] = Show.fromToString
    m.tiemap(KebsDoobie.each[Option[M], Option[A]](KebsDoobie.option(instanceConverter.decodeEither)))(
      _.map(_.map(instanceConverter.encode))
    )
  }
}

private object KebsDoobie {
  def each[M, A: ClassTag](from: M => Either[String, A]): Array[M] => Either[String, Array[A]] = values => {
    val result = new Array[A](values.length)
    var index  = 0
    var error  = Option.empty[String]
    while (index < values.length && error.isEmpty) {
      from(values(index)) match {
        case Right(value)  => result(index) = value
        case Left(message) => error = Some(message)
      }
      index += 1
    }
    error.toLeft(result)
  }

  def option[M, A](from: M => Either[String, A]): Option[M] => Either[String, Option[A]] = {
    case Some(value) => from(value).map(Some(_))
    case None        => Right(None)
  }
}
