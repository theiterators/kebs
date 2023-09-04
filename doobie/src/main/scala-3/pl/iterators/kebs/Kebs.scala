package pl.iterators.kebs

import doobie.{Get, Put, Meta}
import pl.iterators.kebs.enums.KebsEnums
import pl.iterators.kebs.instances.InstanceConverter
import pl.iterators.kebs.macros.ValueClassLike

import scala.reflect.ClassTag

trait Kebs {
  inline given[A, M](using vcLike: ValueClassLike[A, M], m: Meta[M]): Meta[A] = m.imap(vcLike.apply)(vcLike.unapply)

  inline given[A, M](using vcLike: ValueClassLike[A, M], m: Meta[Option[M]]): Meta[Option[A]] = m.imap(_.map(vcLike.apply))(_.map(vcLike.unapply))

  inline given[A, M](using vcLike: ValueClassLike[A, M], m: Meta[Array[M]], cta: ClassTag[A], ctm: ClassTag[M]): Meta[Array[A]] = m.imap(_.map(vcLike.apply))(_.map(vcLike.unapply))

  inline given[A, M](using vcLike: ValueClassLike[A, M], m: Meta[Array[Option[M]]], cta: ClassTag[Option[A]]): Meta[Array[Option[A]]] = m.imap(_.map(_.map(vcLike.apply)))(_.map(_.map(vcLike.unapply)))

  inline given[A, M](using instanceConverter: InstanceConverter[A, M], m: Meta[M]): Meta[A] = m.imap(instanceConverter.decode)(instanceConverter.encode)

  inline given[A, M](using instanceConverter: InstanceConverter[A, M], m: Meta[Array[M]], cta: ClassTag[A], ctm: ClassTag[M]): Meta[Array[A]] = m.imap(_.map(instanceConverter.decode))(_.map(instanceConverter.encode))

  inline given[A, M](using instanceConverter: InstanceConverter[A, M], m: Meta[Array[Option[M]]], cta: ClassTag[Option[A]]): Meta[Array[Option[A]]] = m.imap(_.map(_.map(instanceConverter.decode)))(_.map(_.map(instanceConverter.encode)))
}