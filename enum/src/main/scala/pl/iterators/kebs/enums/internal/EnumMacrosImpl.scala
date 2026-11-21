package pl.iterators.kebs.enums.internal

import pl.iterators.kebs.core.enums.EnumLike
import pl.iterators.kebs.core.macros.internal.EnumCasesMacrosImpl

/** Shared (Scala 2.13 + Scala 3) derivation for `scala.Enumeration`, Scala 3 enums and sealed value enums.
  *
  * Names are the cases' `toString` (kebs 2 semantics). `ValueEnumLike[V, E <: ValueEnumLikeEntry[V]]` has a bounded parameter, which Scala
  * 2 cross-quotes cannot express, so the entry points build it from [[enumCases]].
  */
private[kebs] trait EnumMacrosImpl extends EnumCasesMacrosImpl { this: hearth.MacroCommons =>

  def enumCases[E: Type]: Expr[List[E]] =
    caseObjectsOf[E].getOrElse(
      Environment.reportErrorAndAbort(s"${Type[E].prettyPrint} must be an enum whose cases are all objects (no parameters)")
    )

  def deriveEnumLike[E: Type]: Expr[EnumLike[E]] =
    Expr.quote(EnumLike[E](Expr.splice(enumCases[E]), (e: E) => e.toString))
}
