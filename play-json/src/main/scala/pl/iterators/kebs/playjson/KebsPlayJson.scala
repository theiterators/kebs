package pl.iterators.kebs.playjson

import pl.iterators.kebs.core.instances.InstanceConverter
import pl.iterators.kebs.core.macros.ValueClassLike
import play.api.libs.json._

trait KebsPlayJson {
  implicit def flatReads[T, A](implicit rep: ValueClassLike[T, A], reads: Reads[A]): Reads[T] = reads.map(rep.apply)
  implicit def flatWrites[T, B](implicit rep: ValueClassLike[T, B], writes: Writes[B]): Writes[T] =
    Writes((obj: T) => writes.writes(rep.unapply(obj)))

  implicit def instanceConverterReads[T, A](implicit rep: InstanceConverter[T, A], reads: Reads[A]): Reads[T] = reads.map(rep.decode)
  implicit def instanceConverterWrites[T, B](implicit rep: InstanceConverter[T, B], writes: Writes[B]): Writes[T] =
    Writes((obj: T) => writes.writes(rep.encode(obj)))
}
