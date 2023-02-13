package pl.iterators.kebs

import doobie.{Get, Put, Meta}
import pl.iterators.kebs.enums.KebsEnums
import pl.iterators.kebs.converters.InstanceConverter
import pl.iterators.kebs.macros.CaseClass1Rep

import scala.reflect.ClassTag

trait Kebs {
  inline given[A, M](using cc1Rep: CaseClass1Rep[A, M], m: Meta[M]): Meta[A] = m.imap(cc1Rep.apply)(cc1Rep.unapply)

  inline given[A, M](using cc1Rep: CaseClass1Rep[A, M], m: Meta[Option[M]]): Meta[Option[A]] = m.imap(_.map(cc1Rep.apply))(_.map(cc1Rep.unapply))

  inline given[A, M](using cc1Rep: CaseClass1Rep[A, M], m: Meta[Array[M]], cta: ClassTag[A], ctm: ClassTag[M]): Meta[Array[A]] = m.imap(_.map(cc1Rep.apply))(_.map(cc1Rep.unapply))

  inline given[A, M](using cc1Rep: CaseClass1Rep[A, M], m: Meta[Array[Option[M]]], cta: ClassTag[Option[A]]): Meta[Array[Option[A]]] = m.imap(_.map(_.map(cc1Rep.apply)))(_.map(_.map(cc1Rep.unapply)))

  inline given[A, M](using instanceConverter: InstanceConverter[A, M], m: Meta[M]): Meta[A] = m.imap(instanceConverter.decode)(instanceConverter.encode)

  inline given[A, M](using instanceConverter: InstanceConverter[A, M], m: Meta[Array[M]], cta: ClassTag[A], ctm: ClassTag[M]): Meta[Array[A]] = m.imap(_.map(instanceConverter.decode))(_.map(instanceConverter.encode))

  inline given[A, M](using instanceConverter: InstanceConverter[A, M], m: Meta[Array[Option[M]]], cta: ClassTag[Option[A]]): Meta[Array[Option[A]]] = m.imap(_.map(_.map(instanceConverter.decode)))(_.map(_.map(instanceConverter.encode)))
}