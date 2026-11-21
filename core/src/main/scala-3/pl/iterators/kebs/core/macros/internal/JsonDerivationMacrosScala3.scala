package pl.iterators.kebs.core.macros.internal

import scala.deriving.Mirror
import scala.quoted.*

private[kebs] trait JsonDerivationMacrosScala3 extends JsonDerivationMacrosImpl { this: hearth.MacroCommonsScala3 =>

  override protected def constructViaMirror[F: Type](arguments: List[Expr_??]): Option[Expr[F]] = {
    given Quotes = quotes
    scala.quoted.Expr.summon[Mirror.ProductOf[F]].map { mirror =>
      val values = Varargs(arguments.map(_.value.asInstanceOf[scala.quoted.Expr[Any]]))
      '{ $mirror.fromProduct(Tuple.fromArray(Array[Any]($values*))).asInstanceOf[F] }
    }
  }
}
