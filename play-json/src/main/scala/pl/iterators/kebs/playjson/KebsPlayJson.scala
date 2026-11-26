package pl.iterators.kebs.playjson

import pl.iterators.kebs.core.instances.InstanceConverter
import pl.iterators.kebs.core.json.{JsonConfig, NoneHandling}
import pl.iterators.kebs.core.macros.ValueClassLike
import pl.iterators.kebs.core.naming.Naming
import play.api.libs.json._

// all of KebsPlayJson but its implicit derivation: instances come from deriveReads, deriveWrites and deriveFormat
trait KebsPlayJsonSemiauto extends KebsPlayJsonDeriveCompat {
  // read through the protocol, not implicitly: another protocol imported in the same scope would make it ambiguous
  def kebsJsonConfig: JsonConfig = JsonConfig.default.withNoneHandling(NoneHandling.Omit)

  implicit def flatReads[T, A](implicit rep: ValueClassLike[T, A], reads: Reads[A]): Reads[T] = reads.flatMapResult { obj =>
    rep.from(obj).fold(JsError(_), JsSuccess(_))
  }
  implicit def flatWrites[T, B](implicit rep: ValueClassLike[T, B], writes: Writes[B]): Writes[T] =
    Writes((obj: T) => writes.writes(rep.unapply(obj)))

  implicit def instanceConverterReads[T, A](implicit rep: InstanceConverter[T, A], reads: Reads[A]): Reads[T] = reads.flatMapResult {
    obj =>
      rep.decodeEither(obj).fold(JsError(_), JsSuccess(_))

  }
  implicit def instanceConverterWrites[T, B](implicit rep: InstanceConverter[T, B], writes: Writes[B]): Writes[T] =
    Writes((obj: T) => writes.writes(rep.encode(obj)))
}

trait KebsPlayJson extends KebsPlayJsonSemiauto with KebsPlayJsonAutoCompat {
  // defined in a subclass of KebsPlayJsonAutoCompat, so that they win by owner
  implicit override def flatReads[T, A](implicit rep: ValueClassLike[T, A], reads: Reads[A]): Reads[T] =
    super.flatReads[T, A](rep, reads)
  implicit override def flatWrites[T, B](implicit rep: ValueClassLike[T, B], writes: Writes[B]): Writes[T] =
    super.flatWrites[T, B](rep, writes)
  implicit override def instanceConverterReads[T, A](implicit rep: InstanceConverter[T, A], reads: Reads[A]): Reads[T] =
    super.instanceConverterReads[T, A](rep, reads)
  implicit override def instanceConverterWrites[T, B](implicit rep: InstanceConverter[T, B], writes: Writes[B]): Writes[T] =
    super.instanceConverterWrites[T, B](rep, writes)

  trait KebsPlayJsonSnakified extends KebsPlayJson {
    override def kebsJsonConfig: JsonConfig = super.kebsJsonConfig.withFieldNaming(Naming.snakeCase)
  }
  trait KebsPlayJsonCapitalized extends KebsPlayJson {
    override def kebsJsonConfig: JsonConfig = super.kebsJsonConfig.withFieldNaming(Naming.pascalCase)
  }
}
