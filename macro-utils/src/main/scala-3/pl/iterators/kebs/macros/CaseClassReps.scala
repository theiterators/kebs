package pl.iterators.kebs.macros

final class CaseClass1Rep[CC, F1](val apply: F1 => CC, val unapply: CC => F1)
