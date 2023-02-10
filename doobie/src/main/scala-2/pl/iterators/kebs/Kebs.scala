package pl.iterators.kebs

import doobie.Meta
import pl.iterators.kebs.instances.InstanceConverter
import pl.iterators.kebs.macros.base.CaseClass1Rep

import scala.reflect.ClassTag

trait Kebs {
  implicit def caseClass1RepMeta[A, M](implicit cc1Rep: CaseClass1Rep[A, M], m: Meta[M]): Meta[A] = m.imap(cc1Rep.apply)(cc1Rep.unapply)

  implicit def caseClass1RepArrayMeta[A, M](implicit cc1Rep: CaseClass1Rep[A, M], m: Meta[Array[M]], cta: ClassTag[A], ctm: ClassTag[M]): Meta[Array[A]] = m.imap(_.map(cc1Rep.apply))(_.map(cc1Rep.unapply))

  implicit def caseClass1RepOptionArrayMeta[A, M](implicit cc1Rep: CaseClass1Rep[A, M], m: Meta[Array[Option[M]]], cta: ClassTag[A], ctm: ClassTag[M]): Meta[Array[Option[A]]] = m.imap(_.map(_.map(cc1Rep.apply)))(_.map(_.map(cc1Rep.unapply)))

  implicit def instanceConverterMeta[A, M](implicit  instanceConverter: InstanceConverter[A, M], m: Meta[M]): Meta[A] = m.imap(instanceConverter.decode)(instanceConverter.encode)

  implicit def instanceConverterArrayMeta[A, M](implicit instanceConverter: InstanceConverter[A, M], m: Meta[Array[M]], cta: ClassTag[A], ctm: ClassTag[M]): Meta[Array[A]] = m.imap(_.map(instanceConverter.decode))(_.map(instanceConverter.encode))

  implicit def instanceConverterOptionArrayMeta[A, M](implicit instanceConverter: InstanceConverter[A, M], m: Meta[Array[Option[M]]], cta: ClassTag[A], ctm: ClassTag[M]): Meta[Array[Option[A]]] = m.imap(_.map(_.map(instanceConverter.decode)))(_.map(_.map(instanceConverter.encode)))
}