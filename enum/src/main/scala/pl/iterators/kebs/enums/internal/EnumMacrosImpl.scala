package pl.iterators.kebs.enums.internal

import pl.iterators.kebs.core.enums.EnumLike
import pl.iterators.kebs.core.macros.internal.EnumCasesMacrosImpl

private[kebs] trait EnumMacrosImpl extends EnumCasesMacrosImpl { this: hearth.MacroCommons =>

  // ValueEnumLike is built by the entry points: Scala 2 cross-quotes cannot express its bounded parameter
  def enumCases[E: Type]: Expr[List[E]] =
    caseObjectsOf[E].getOrElse(
      Environment.reportErrorAndAbort(s"${Type[E].prettyPrint} must be an enum whose cases are all objects (no parameters)")
    )

  // names are toString, as in kebs 2
  def deriveEnumLike[E: Type]: Expr[EnumLike[E]] =
    Expr.quote(EnumLike[E](Expr.splice(enumCases[E]), (e: E) => e.toString))
}
