package pl.iterators.kebs.enumeratum.internal

import enumeratum.EnumEntry
import pl.iterators.kebs.core.enums.EnumLike
import pl.iterators.kebs.core.macros.internal.EnumCasesMacrosImpl

/** Shared (Scala 2.13 + Scala 3) derivation of enumeratum entries and `EnumLike`.
  *
  * Entries come from the companion's own `values` (so a user-defined `values` is respected); `fallback` is used only when the companion
  * has no `values` returning a `Seq` of the entry type. (Types with bounded parameters, such as
  * `ValueEnumLike[V, E <: ValueEnumLikeEntry[V]]`, cannot appear inside Scala 2 cross-quotes - the entry points build those from the
  * entries.)
  */
private[kebs] trait EnumeratumMacrosImpl extends EnumCasesMacrosImpl { this: hearth.MacroCommons =>

  /** Entries of `E`: the companion's `values`; with a `fallback` (Scala 3) the fallback is also used when `values` turns out empty at
    * runtime - enumeratum's Scala 3 `findValues` finds nothing for an enum declared inside a class.
    */
  def enumeratumEntries[E: Type](fallback: Option[Expr[List[E]]]): Expr[List[E]] =
    (companionValues[E], fallback) match {
      case (Some(values), Some(caseObjects)) =>
        Expr.quote {
          val fromCompanion = Expr.splice(values)
          if (fromCompanion.nonEmpty) fromCompanion else Expr.splice(caseObjects)
        }
      case (Some(values), None)      => values
      case (None, Some(caseObjects)) => caseObjects
      case (None, None)              =>
        Environment.reportErrorAndAbort(s"${Type[E].prettyPrint} has no companion object extending enumeratum.Enum or ValueEnum")
    }

  def deriveEnumLike[E: Type](entries: Expr[List[E]]): Expr[EnumLike[E]] = {
    implicit val EnumEntryType: Type[EnumEntry] = Type.of[EnumEntry]
    Expr.quote {
      EnumLike[E](Expr.splice(entries), (e: E) => Expr.splice(Expr.quote(e).upcast[EnumEntry]).entryName)
    }
  }

  /** `E.values.toList` when `E`'s companion has a `values` member returning a `Seq[E]` (enumeratum `Enum`/`ValueEnum`). */
  private def companionValues[E: Type]: Option[Expr[List[E]]] = {
    implicit val SeqOfE: Type[Seq[E]] = Type.of[Seq[E]]
    Type.companionObject[E].flatMap { companion =>
      import companion.{Underlying as Companion, value as companionExpr}
      Type[Companion]
        .unsortedMethodsNamed("values")
        .iterator
        .filter(_.isAvailable(AtCallSite))
        .map((method: Method) => method)
        .map {
          case onInstance: Method.OnInstance =>
            onInstance.apply(companionExpr.asInstanceOf[Expr[onInstance.Instance]]) match {
              case result: Method.Result[?] =>
                import result.Returned
                if (Returned <:< Type[Seq[E]])
                  result.build().toOption.map(values => Expr.quote(Expr.splice(values.asInstanceOf[Expr[Seq[E]]]).toList))
                else None
              case _ => None
            }
          case _ => None
        }
        .collectFirst { case Some(values) => values }
    }
  }
}
