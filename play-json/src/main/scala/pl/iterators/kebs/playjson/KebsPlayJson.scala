package pl.iterators.kebs.playjson

import pl.iterators.kebs.core.instances.InstanceConverter
import pl.iterators.kebs.core.macros.ValueClassLike
import play.api.libs.json._

import scala.util.Try

trait KebsPlayJson {
  implicit def flatReads[T, A](implicit rep: ValueClassLike[T, A], reads: Reads[A]): Reads[T] = reads.flatMapResult { obj =>
    Try(rep.apply(obj)).toEither.left.map(e => JsError(e.getMessage)).map(JsSuccess(_)).merge
  }
  implicit def flatWrites[T, B](implicit rep: ValueClassLike[T, B], writes: Writes[B]): Writes[T] =
    Writes((obj: T) => writes.writes(rep.unapply(obj)))

  implicit def instanceConverterReads[T, A](implicit rep: InstanceConverter[T, A], reads: Reads[A]): Reads[T] = reads.flatMapResult {
    obj =>
      Try(rep.decode(obj)).toEither.left.map(e => JsError(e.getMessage)).map(JsSuccess(_)).merge

  }
  implicit def instanceConverterWrites[T, B](implicit rep: InstanceConverter[T, B], writes: Writes[B]): Writes[T] =
    Writes((obj: T) => writes.writes(rep.encode(obj)))
}
