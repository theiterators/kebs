package pl.iterators.kebs.core.macros.internal

import hearth.fp.effect.MIO
import hearth.fp.instances.*
import hearth.fp.syntax.*
import pl.iterators.kebs.core.macros.ValueClassLike

/** Shared (Scala 2.13 + Scala 3) implementation of `ValueClassLike` derivation for single-field case classes. */
private[kebs] trait ValueClassLikeMacrosImpl { this: hearth.MacroCommons =>

  /** Builds `ValueClassLike[T, F]` where `F` is the type of the only field of case class `T`.
    *
    * Values are constructed through the companion's `apply` (so validating/normalising `apply`s are honoured); the primary constructor is
    * used only when no matching `apply` is callable at the call site.
    */
  def deriveValueClassLike[T: Type]: Expr_?? = {
    val caseClass: CaseClass[T] = CaseClass.parse[T].toOption match {
      case Some(cc) => cc
      case None     => Environment.reportErrorAndAbort(s"${Type[T].prettyPrint} is not a case class")
    }
    val field: Parameter = caseClass.primaryConstructor.parameters.flatMap(_.values.toList) match {
      case single :: Nil => single
      case _ => Environment.reportErrorAndAbort(s"To derive ValueClassLike, ${Type[T].prettyPrint} must have exactly one field")
    }
    // a nested path (`field.tpe.Underlying`) breaks cross-quotes on Scala 2.13 - bind it first
    val fieldType: ?? = field.tpe
    import fieldType.Underlying as F
    implicit val ValueClassLikeType: Type[ValueClassLike[T, F]] = Type.of[ValueClassLike[T, F]]

    def construct(arg: Expr[F]): Expr[T] =
      viaCompanionApply[T, F](arg).getOrElse {
        caseClass.construct[MIO](_ => MIO.pure(arg.as_??)).unsafe.runSync._2 match {
          case Right(Some(constructed)) => constructed
          case _ => Environment.reportErrorAndAbort(s"${Type[T].prettyPrint} has no accessible apply or constructor")
        }
      }

    def access(value: Expr[T]): Expr[F] = {
      val fieldValue = caseClass.caseFieldValuesAt(value).head._2
      import fieldValue.{Underlying as G, value as extracted}
      extracted.asInstanceOf[Expr[F]]
    }

    Expr.quote {
      new ValueClassLike[T, F](
        (f: F) => Expr.splice(construct(Expr.quote(f))),
        (t: T) => Expr.splice(access(Expr.quote(t)))
      )
    }.as_??
  }

  /** Calls `T.apply(arg)` if the companion has an `apply` taking exactly one argument of type `F`. */
  private def viaCompanionApply[T: Type, F: Type](arg: Expr[F]): Option[Expr[T]] = {
    def walk(method: Method): Option[Expr[T]] = method match {
      case _: Method.OnInstance     => None
      case types: Method.ApplyTypes =>
        val typeParams = types.typeParams.flatten
        val typeArgs   = Type.typeArguments[T]
        if (typeParams.size == typeArgs.size) walk(types.apply(typeParams.zip(typeArgs).toMap)) else None
      case values: Method.ApplyValues =>
        values.parameters.flatMap(_.toList) match {
          case (name, param) :: Nil if param.tpe.Underlying =:= Type[F] => walk(values.apply(Map(name -> arg.as_??)))
          case _                                                        => None
        }
      case result: Method.Result[?] =>
        import result.Returned
        if (Returned <:< Type[T]) result.build().toOption.map(_.asInstanceOf[Expr[T]]) else None
    }
    Type[T].unsortedMethodsNamed("apply").iterator.filter(_.isAvailable(AtCallSite)).map(walk).collectFirst { case Some(e) => e }
  }
}
