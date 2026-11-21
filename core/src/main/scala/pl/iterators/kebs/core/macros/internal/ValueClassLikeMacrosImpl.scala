package pl.iterators.kebs.core.macros.internal

import hearth.fp.effect.MIO
import hearth.fp.instances.*
import hearth.fp.syntax.*
import pl.iterators.kebs.core.macros.ValueClassLike

private[kebs] trait ValueClassLikeMacrosImpl { this: hearth.MacroCommons =>

  final class ProductFallback[T, F](val construct: Expr[F] => Expr[T], val access: Expr[T] => Expr[F])

  def deriveValueClassLike[T: Type]: Expr_?? = {
    val caseClass: CaseClass[T] = CaseClass.parse[T].toOption match {
      case Some(cc) => cc
      case None     => Environment.reportErrorAndAbort(s"${Type[T].prettyPrint} is not a case class")
    }
    val field: Parameter = singleField(caseClass).getOrElse(
      Environment.reportErrorAndAbort(s"To derive ValueClassLike, ${Type[T].prettyPrint} must have exactly one field")
    )
    // a nested path (field.tpe.Underlying) breaks cross-quotes on 2.13
    val fieldType: ?? = field.tpe
    import fieldType.Underlying as F
    implicit val ValueClassLikeType: Type[ValueClassLike[T, F]] = Type.of[ValueClassLike[T, F]]
    build[T, F](Some(caseClass), None).as_??
  }

  def deriveValueClassLikeWithFallback[T: Type, F: Type](fallback: ProductFallback[T, F]): Expr[ValueClassLike[T, F]] = {
    val caseClass = CaseClass.parse[T].toOption.filter(cc => singleField(cc).exists(_.tpe.Underlying =:= Type[F]))
    build[T, F](caseClass, Some(fallback))
  }

  private def singleField[T](caseClass: CaseClass[T]): Option[Parameter] =
    caseClass.primaryConstructor.parameters.flatMap(_.values.toList) match {
      case single :: Nil => Some(single)
      case _             => None
    }

  private def build[T: Type, F: Type](
      caseClass: Option[CaseClass[T]],
      fallback: Option[ProductFallback[T, F]]
  ): Expr[ValueClassLike[T, F]] = {
    // companion apply first, so that validating/normalising applys are honoured
    def construct(arg: Expr[F]): Expr[T] =
      viaCompanionApply[T, F](arg)
        .orElse(caseClass.flatMap(viaConstructor[T, F](_, arg)))
        .orElse(fallback.map(_.construct(arg)))
        .getOrElse(Environment.reportErrorAndAbort(s"${Type[T].prettyPrint} has no accessible apply or constructor"))

    def access(value: Expr[T]): Expr[F] =
      caseClass
        .flatMap(_.caseFieldValuesAt(value).headOption)
        .map { case (_, fieldValue) =>
          import fieldValue.{Underlying as G, value as extracted}
          extracted.asInstanceOf[Expr[F]]
        }
        .orElse(fallback.map(_.access(value)))
        .getOrElse(Environment.reportErrorAndAbort(s"The field of ${Type[T].prettyPrint} is not accessible"))

    Expr.quote {
      new ValueClassLike[T, F](
        (f: F) => Expr.splice(construct(Expr.quote(f))),
        (t: T) => Expr.splice(access(Expr.quote(t)))
      )
    }
  }

  private def viaConstructor[T: Type, F: Type](caseClass: CaseClass[T], arg: Expr[F]): Option[Expr[T]] =
    caseClass.construct[MIO](_ => MIO.pure(arg.as_??)).unsafe.runSync._2 match {
      case Right(Some(constructed)) => Some(constructed)
      case _                        => None
    }

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
