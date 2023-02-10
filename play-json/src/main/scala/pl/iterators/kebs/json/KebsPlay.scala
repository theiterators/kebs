package pl.iterators.kebs.json

import pl.iterators.kebs.instances.InstanceConverter
import pl.iterators.kebs.macros.CaseClass1Rep
import play.api.libs.json._

trait KebsPlay {
  implicit def flatReads[T, A](implicit rep: CaseClass1Rep[T, A], reads: Reads[A]): Reads[T] = reads.map(rep.apply)
  implicit def flatWrites[T, B](implicit rep: CaseClass1Rep[T, B], writes: Writes[B]): Writes[T] =
    Writes((obj: T) => writes.writes(rep.unapply(obj)))

  implicit def instanceConverterReads[T, A](implicit rep: InstanceConverter[T, A], reads: Reads[A]): Reads[T] = reads.map(rep.decode)
  implicit def instanceConverterWrites[T, B](implicit rep: InstanceConverter[T, B], writes: Writes[B]): Writes[T] =
    Writes((obj: T) => writes.writes(rep.encode(obj)))
}
