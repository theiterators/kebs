package pl.iterators.kebs.circe

import io.circe.`export`.Exported
import io.circe.{Decoder, Encoder}
import pl.iterators.kebs.core.json.JsonConfig

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

// Exported: circe's low-priority hook, so instances in a type's companion win over derivation
private[circe] trait KebsCirceAutoCompat {
  // format: off
  implicit def kebsAutoEncoder[A](implicit config: JsonConfig): Exported[Encoder[A]] = macro KebsCirceMacros.autoEncoder[A]
  implicit def kebsAutoAsObjectEncoder[A](implicit config: JsonConfig): Exported[Encoder.AsObject[A]] = macro KebsCirceMacros.autoAsObjectEncoder[A]
  implicit def kebsAutoDecoder[A](implicit config: JsonConfig): Exported[Decoder[A]] = macro KebsCirceMacros.autoDecoder[A]
  // format: on
}

final private[circe] class KebsCirceMacros(val c: whitebox.Context) {
  import c.universe._

  private def derivable(A: Type): Unit = {
    val symbol = A.typeSymbol
    if (!(symbol.isClass && (symbol.asClass.isCaseClass || symbol.asClass.isSealed)))
      c.abort(c.enclosingPosition, s"$A is not a case class or a sealed type")
  }

  private def kindlings(config: c.Tree) = q"_root_.pl.iterators.kebs.circe.internal.KebsCirceConfig.toKindlings($config)"

  def autoEncoder[A: c.WeakTypeTag](config: c.Tree): c.Tree = {
    val A = weakTypeOf[A]
    derivable(A)
    q"_root_.io.circe.`export`.Exported[_root_.io.circe.Encoder[$A]](_root_.hearth.kindlings.circederivation.KindlingsEncoder.derived[$A](${kindlings(config)}))"
  }

  def autoAsObjectEncoder[A: c.WeakTypeTag](config: c.Tree): c.Tree = {
    val A = weakTypeOf[A]
    derivable(A)
    q"_root_.io.circe.`export`.Exported[_root_.io.circe.Encoder.AsObject[$A]](_root_.hearth.kindlings.circederivation.KindlingsEncoder.deriveAsObject[$A](${kindlings(config)}))"
  }

  def autoDecoder[A: c.WeakTypeTag](config: c.Tree): c.Tree = {
    val A = weakTypeOf[A]
    derivable(A)
    q"_root_.io.circe.`export`.Exported[_root_.io.circe.Decoder[$A]](_root_.hearth.kindlings.circederivation.KindlingsDecoder.derived[$A](${kindlings(config)}))"
  }
}
