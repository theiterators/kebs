package pl.iterators.kebs.circe

import io.circe.`export`.Exported
import io.circe.{Decoder, Encoder}
import pl.iterators.kebs.circe.internal.CirceMacrosImpl
import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.macros.internal.{EntryPointRole, FreshQuotes, JsonDerivationMacrosScala3, LocalInstances}

import scala.deriving.Mirror
import scala.quoted.*

// Exported: circe's low-priority hook, so instances in a type's companion win over derivation
private[circe] trait KebsCirceAutoCompat { this: KebsCirce =>
  inline implicit def kebsAutoEncoder[A](using m: Mirror.Of[A]): Exported[Encoder.AsObject[A]] =
    Exported(KebsCirceMacros.autoEncoder[A](kebsJsonConfig))

  inline implicit def kebsAutoDecoder[A](using m: Mirror.Of[A]): Exported[Decoder[A]] =
    Exported(KebsCirceMacros.autoDecoder[A](kebsJsonConfig))
}

final private[circe] class KebsCirceMacros(q: Quotes)
    extends hearth.MacroCommonsScala3(using q),
      JsonDerivationMacrosScala3,
      CirceMacrosImpl {

  def auto[A](body: => A): A = implicitEntry(body)

  def encodersNeeded[A: Type](config: Expr[JsonConfig]): LocalInstances.Needed = untyped(neededEncoders[A](config))
  def decodersNeeded[A: Type](config: Expr[JsonConfig]): LocalInstances.Needed = untyped(neededDecoders[A](config))
}

private[circe] object KebsCirceMacros {
  inline def autoEncoder[A](inline config: JsonConfig): Encoder.AsObject[A] = ${ autoEncoderImpl[A]('config) }
  inline def autoDecoder[A](inline config: JsonConfig): Decoder[A]          = ${ autoDecoderImpl[A]('config) }
  inline def oneEncoder[A](inline config: JsonConfig): Encoder.AsObject[A]  = ${ oneEncoderImpl[A]('config) }
  inline def oneDecoder[A](inline config: JsonConfig): Decoder[A]           = ${ oneDecoderImpl[A]('config) }

  private def macros(using q: Quotes): KebsCirceMacros = new KebsCirceMacros(FreshQuotes(q))

  def autoEncoderImpl[A: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[Encoder.AsObject[A]] = {
    val m = macros
    m.auto {
      m.encoderRole[A](config) match {
        case EntryPointRole.Record => m.recordedEncoder[A](config)
        case EntryPointRole.Derive => block[Encoder.AsObject[A]](config, m.encodersNeeded[A](config))
        case _                     => stepAside[A]
      }
    }
  }

  def autoDecoderImpl[A: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[Decoder[A]] = {
    val m = macros
    m.auto {
      m.decoderRole[A](config) match {
        case EntryPointRole.Record => m.recordedDecoder[A](config)
        case EntryPointRole.Derive => block[Decoder[A]](config, m.decodersNeeded[A](config))
        case _                     => stepAside[A]
      }
    }
  }

  private def stepAside[A: Type](using Quotes): Nothing =
    quotes.reflect.report.errorAndAbort(s"${Type.show[A]} is not derived here")

  private def block[R: Type](config: Expr[JsonConfig], needed: LocalInstances.Needed)(using Quotes): Expr[R] =
    LocalInstances.block[R](config, needed)(
      new LocalInstances.Define {
        def apply[X: Type](instance: LocalInstances.Untyped, config: Expr[JsonConfig])(using Quotes): LocalInstances.Definition[?] =
          if (instance.typeClass == "Encoder") new LocalInstances.Definition('{ KebsCirceMacros.oneEncoder[X]($config) })
          else new LocalInstances.Definition('{ KebsCirceMacros.oneDecoder[X]($config) })
      }
    )(None)

  def oneEncoderImpl[A: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[Encoder.AsObject[A]] = macros.encoderLevel[A](config)

  def oneDecoderImpl[A: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[Decoder[A]] = macros.decoderLevel[A](config)
}
