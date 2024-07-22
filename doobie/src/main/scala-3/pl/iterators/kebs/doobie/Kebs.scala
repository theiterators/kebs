package pl.iterators.kebs.doobie

import doobie.Meta
import pl.iterators.kebs.core.instances.InstanceConverter
import pl.iterators.kebs.core.macros.ValueClassLike

import scala.reflect.ClassTag

trait Kebs {
  inline implicit def metaFromValueClassLike[A, M](using vcLike: ValueClassLike[A, M], m: Meta[M]): Meta[A] =
    m.imap(vcLike.apply)(vcLike.unapply)

  inline implicit def metaArrayFromValueClassLike[A, M](using
      vcLike: ValueClassLike[A, M],
      m: Meta[Array[M]],
      cta: ClassTag[A],
      ctm: ClassTag[M]
  ): Meta[Array[A]] =
    m.imap(_.map(vcLike.apply))(_.map(vcLike.unapply))

  inline implicit def metaArrayOptionFromValueClassLike[A, M](using
      vcLike: ValueClassLike[A, M],
      m: Meta[Array[Option[M]]],
      cta: ClassTag[Option[A]]
  ): Meta[Array[Option[A]]] =
    m.imap(_.map(_.map(vcLike.apply)))(_.map(_.map(vcLike.unapply)))

  inline implicit def metaFromInstanceConverter[A, M](using instanceConverter: InstanceConverter[A, M], m: Meta[M]): Meta[A] =
    m.imap(instanceConverter.decode)(instanceConverter.encode)

  inline implicit def metaArrayFromInstanceConverter[A, M](using
      instanceConverter: InstanceConverter[A, M],
      m: Meta[Array[M]],
      cta: ClassTag[A],
      ctm: ClassTag[M]
  ): Meta[Array[A]] = m.imap(_.map(instanceConverter.decode))(_.map(instanceConverter.encode))

  inline implicit def metaArrayOptionFromInstanceConverter[A, M](using
      instanceConverter: InstanceConverter[A, M],
      m: Meta[Array[Option[M]]],
      cta: ClassTag[Option[A]]
  ): Meta[Array[Option[A]]] = m.imap(_.map(_.map(instanceConverter.decode)))(_.map(_.map(instanceConverter.encode)))
}
