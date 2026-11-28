package pl.iterators.kebs.core.macros

final class ValueClassLike[VC, F1](val apply: F1 => VC, val unapply: VC => F1)

trait CaseClass1ToValueClass  extends CaseClass1ToValueClassCompat
object CaseClass1ToValueClass extends CaseClass1ToValueClass
