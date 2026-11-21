package pl.iterators.kebs.enumeratum.internal

import enumeratum.EnumEntry
import pl.iterators.kebs.core.enums.EnumLike
import pl.iterators.kebs.core.macros.internal.EnumCasesMacrosImpl

private[kebs] trait EnumeratumMacrosImpl extends EnumCasesMacrosImpl { this: hearth.MacroCommons =>

  def enumeratumEntries[E: Type](fallback: Option[Expr[List[E]]]): Expr[List[E]] =
    (companionValues[E], fallback) match {
      case (Some(values), Some(caseObjects)) =>
        Expr.quote {
          val fromCompanion = Expr.splice(values)
          // enumeratum's Scala 3 findValues finds nothing for an enum declared inside a class
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
