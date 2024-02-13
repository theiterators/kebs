package pl.iterators.kebs

import doobie.Meta
import pl.iterators.kebs.instances.InstanceConverter
import pl.iterators.kebs.macros.{ValueClassLike, FlatCaseClass1}

import scala.reflect.ClassTag

trait Kebs extends FlatCaseClass1 {
  implicit def ValueClassLikeMeta[A, M](implicit vcLike: ValueClassLike[A, M], m: Meta[M]): Meta[A] = m.imap(vcLike.apply)(vcLike.unapply)

  implicit def ValueClassLikeArrayMeta[A, M](implicit vcLike: ValueClassLike[A, M], m: Meta[Array[M]], cta: ClassTag[A], ctm: ClassTag[M]): Meta[Array[A]] = m.imap(_.map(vcLike.apply))(_.map(vcLike.unapply))

  implicit def ValueClassLikeOptionArrayMeta[A, M](implicit vcLike: ValueClassLike[A, M], m: Meta[Array[Option[M]]], cta: ClassTag[A], ctm: ClassTag[M]): Meta[Array[Option[A]]] = m.imap(_.map(_.map(vcLike.apply)))(_.map(_.map(vcLike.unapply)))

  implicit def instanceConverterMeta[A, M](implicit  instanceConverter: InstanceConverter[A, M], m: Meta[M]): Meta[A] = m.imap(instanceConverter.decode)(instanceConverter.encode)

  implicit def instanceConverterArrayMeta[A, M](implicit instanceConverter: InstanceConverter[A, M], m: Meta[Array[M]], cta: ClassTag[A], ctm: ClassTag[M]): Meta[Array[A]] = m.imap(_.map(instanceConverter.decode))(_.map(instanceConverter.encode))

  implicit def instanceConverterOptionArrayMeta[A, M](implicit instanceConverter: InstanceConverter[A, M], m: Meta[Array[Option[M]]], cta: ClassTag[A], ctm: ClassTag[M]): Meta[Array[Option[A]]] = m.imap(_.map(_.map(instanceConverter.decode)))(_.map(_.map(instanceConverter.encode)))
}