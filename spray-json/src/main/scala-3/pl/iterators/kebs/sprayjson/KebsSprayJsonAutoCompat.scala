package pl.iterators.kebs.sprayjson

import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.sprayjson.internal.SprayFormatMacrosImpl
import spray.json.RootJsonFormat

import scala.deriving.Mirror
import scala.quoted.*

// a parent of KebsSprayJson, so that jsonFlatFormat wins a JsonFormat request for a value class (as in kebs 2)
private[sprayjson] trait KebsSprayJsonAutoCompat { self: KebsSprayJson =>
  inline implicit def jsonFormatN[T](using m: Mirror.ProductOf[T]): RootJsonFormat[T] = KebsSprayJsonMacros.derive[T](kebsJsonConfig)

  @deprecated("jsonFormatN handles recursive types", "3.0.0")
  inline final def jsonFormatRec[T](using m: Mirror.ProductOf[T]): RootJsonFormat[T] = KebsSprayJsonMacros.derive[T](kebsJsonConfig)
}

final private[sprayjson] class KebsSprayJsonMacros(q: Quotes) extends hearth.MacroCommonsScala3(using q), SprayFormatMacrosImpl {
  import quotes.reflect.*

  override protected def isEnclosingDefinition(expr: UntypedExpr): Boolean = {
    def strip(term: Term): Term = term match {
      case Inlined(_, _, inner) => strip(inner)
      case Typed(inner, _)      => strip(inner)
      case other                => other
    }
    val referenced = strip(expr).symbol
    !referenced.isNoSymbol &&
    Iterator.iterate(Symbol.spliceOwner)(_.owner).takeWhile(owner => !owner.isNoSymbol && !owner.isClassDef).contains(referenced)
  }
}

private[sprayjson] object KebsSprayJsonMacros {
  inline def derive[T](inline config: JsonConfig): RootJsonFormat[T] = ${ deriveImpl[T]('config) }

  def deriveImpl[T: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[RootJsonFormat[T]] =
    new KebsSprayJsonMacros(q).deriveRootFormat[T](config)
}
