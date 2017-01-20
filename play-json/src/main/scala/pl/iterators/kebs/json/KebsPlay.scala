package pl.iterators.kebs.json

import pl.iterators.kebs.json.KebsPlay.InvariantDummy
import play.api.libs.json._

trait KebsPlay {
  import macros.KebsPlayMacros
  implicit def flatReads[T <: Product]: Reads[T] = macro KebsPlayMacros.materializeFlatReads[T]
  /*
  Trick to find invariant implicit. See: https://github.com/scalamacros/macrology201/commit/78779cc7f565dde003fe0da9e5357821b009917b
  Without it compiler would have passed WeakTypeTag of upper-bound of `T`, instead of `T`.
  See: https://issues.scala-lang.org/browse/SI-8802
   */
  implicit def flatWrites[T <: Product: InvariantDummy]: Writes[T] = macro KebsPlayMacros.materializeFlatWrites[T]

}

object KebsPlay {
  trait InvariantDummy[T]
  object InvariantDummy {
    implicit def materialize[T]: InvariantDummy[T] = null.asInstanceOf[InvariantDummy[T]]
  }
}
