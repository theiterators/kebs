package pl.iterators.kebs.core.macros

import scala.util.control.NonFatal

// modules read values through `from`, so that a rejected value is their decoding error, not an exception
final class ValueClassLike[VC, F1](val apply: F1 => VC, val unapply: VC => F1, val from: F1 => Either[String, VC]) {
  def this(apply: F1 => VC, unapply: VC => F1) = this(apply, unapply, ValueClassLike.catching(apply))
}

// derived in implicit scope, not where CaseClass1ToValueClass is imported: an instance in the type's companion is more
// specific and wins
object ValueClassLike extends ValueClassLikeCompat {
  def catching[A, B](f: A => B): A => Either[String, B] =
    a =>
      try Right(f(a))
      catch { case NonFatal(e) => Left(Option(e.getMessage).getOrElse(e.toString)) }
}

// a single-field case class whose ValueClassLike kebs derives, so that modules treat it as its field
sealed trait ValueClass[VC]
object ValueClass extends ValueClassCompat {
  private object Marker extends ValueClass[Any]

  // what `derived` returns once the type is checked
  def unchecked[VC]: ValueClass[VC] = Marker.asInstanceOf[ValueClass[VC]]
}

trait CaseClass1ToValueClass {
  implicit def everyValueClass[VC]: ValueClass[VC] = ValueClass.unchecked[VC]
}
object CaseClass1ToValueClass extends CaseClass1ToValueClass
