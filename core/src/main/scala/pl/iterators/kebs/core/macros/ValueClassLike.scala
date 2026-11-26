package pl.iterators.kebs.core.macros

import scala.util.control.NonFatal

// modules read values through `from`, so that a rejected value is their decoding error, not an exception
final class ValueClassLike[VC, F1](val apply: F1 => VC, val unapply: VC => F1, val from: F1 => Either[String, VC]) {
  def this(apply: F1 => VC, unapply: VC => F1) = this(apply, unapply, ValueClassLike.catching(apply))
}

object ValueClassLike {
  def catching[A, B](f: A => B): A => Either[String, B] =
    a =>
      try Right(f(a))
      catch { case NonFatal(e) => Left(Option(e.getMessage).getOrElse(e.toString)) }
}

trait CaseClass1ToValueClass  extends CaseClass1ToValueClassCompat
object CaseClass1ToValueClass extends CaseClass1ToValueClass
