package pl.iterators.kebs.sprayjson

import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.sprayjson.internal.SprayFormatMacrosImpl
import spray.json.RootJsonFormat

import scala.deriving.Mirror
import scala.quoted.*

// a parent of KebsSprayJson, so that jsonFlatFormat wins a JsonFormat request for a value class (as in kebs 2)
private[sprayjson] trait KebsSprayJsonAutoCompat { self: KebsSprayJson =>
  inline implicit def kebsAutoJsonFormat[T](using m: Mirror.ProductOf[T]): RootJsonFormat[T] =
    KebsSprayJsonMacros.auto[T](kebsJsonConfig)

  // transparent and stepping aside when a format exists: a sealed enum also has kebs' enum format, which must win
  transparent inline implicit def kebsAutoJsonFormatSealed[T](using m: Mirror.SumOf[T]): RootJsonFormat[T] =
    KebsSprayJsonMacros.autoSealed[T](kebsJsonConfig)

  inline def jsonFormatN[T]: RootJsonFormat[T]      = KebsSprayJsonMacros.explicit[T](kebsJsonConfig)
  inline def jsonFormatSealed[T]: RootJsonFormat[T] = KebsSprayJsonMacros.explicit[T](kebsJsonConfig)

  @deprecated("jsonFormatN handles recursive types", "3.0.0")
  inline final def jsonFormatRec[T]: RootJsonFormat[T] = KebsSprayJsonMacros.explicit[T](kebsJsonConfig)
}

final private[sprayjson] class KebsSprayJsonMacros(q: Quotes) extends hearth.MacroCommonsScala3(using q), SprayFormatMacrosImpl {

  override protected def constructViaMirror[F: Type](arguments: List[Expr_??]): Option[Expr[F]] = {
    given Quotes = q
    scala.quoted.Expr.summon[Mirror.ProductOf[F]].map { mirror =>
      val values = Varargs(arguments.map(_.value.asInstanceOf[scala.quoted.Expr[Any]]))
      '{ $mirror.fromProduct(Tuple.fromArray(Array[Any]($values*))).asInstanceOf[F] }
    }
  }

  def autoSealed[T: Type](config: Expr[JsonConfig]): Expr[RootJsonFormat[T]] = {
    // spray formats the standard library itself; failing here only skips this candidate
    if (isStandardLibrary[T]) Environment.reportErrorAndAbort(s"${Type[T].prettyPrint} is not derived here")
    deriveRootFormat[T](config, consultExisting = true, stepAside = true)
  }
}

private[sprayjson] object KebsSprayJsonMacros {
  inline def auto[T](inline config: JsonConfig): RootJsonFormat[T]                   = ${ autoImpl[T]('config) }
  transparent inline def autoSealed[T](inline config: JsonConfig): RootJsonFormat[T] = ${ autoSealedImpl[T]('config) }
  inline def explicit[T](inline config: JsonConfig): RootJsonFormat[T]               = ${ explicitImpl[T]('config) }

  def autoImpl[T: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[RootJsonFormat[T]] =
    new KebsSprayJsonMacros(q).deriveRootFormat[T](config, consultExisting = true, stepAside = false)

  def autoSealedImpl[T: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[RootJsonFormat[T]] =
    new KebsSprayJsonMacros(q).autoSealed[T](config)

  def explicitImpl[T: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[RootJsonFormat[T]] =
    new KebsSprayJsonMacros(q).deriveRootFormat[T](config, consultExisting = false, stepAside = false)
}
