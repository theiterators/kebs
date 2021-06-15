package pl.iterators.kebs.circe

final class CaseClass1Rep[CC, F1](val apply: F1 => CC, val unapply: CC => F1)

// object CaseClass1Rep {
//   implicit def repFromCaseClass[CC <: Product, F1]: CaseClass1Rep[CC, F1] = macro CaseClassRepMacros.materializeCaseClass1Rep[CC, F1]
// }