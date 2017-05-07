package pl.iterators.kebs.unmarshallers.enums.macros

import akka.http.scaladsl.unmarshalling.{FromStringUnmarshaller, Unmarshaller}
import pl.iterators.kebs.macros.EnumMacroUtils

import scala.reflect.macros.whitebox

class KebsEnumUnmarshallersMacros(override val c: whitebox.Context) extends EnumMacroUtils {
  import c.universe._

  def materializeEnumUnmarshaller[E: c.WeakTypeTag]: c.Expr[FromStringUnmarshaller[E]] = {
    val EnumEntry = weakTypeOf[E]
    assertEnumEntry(EnumEntry, s"To materialize unmarshaller ${EnumEntry.typeSymbol} must subclass enumeratum.EnumEntry")

    c.Expr[FromStringUnmarshaller[E]](q"${_this}.enumUnmarshaller[$EnumEntry](${companion(EnumEntry)})")
  }

  def materializeValueEnumUnmarshaller[V: c.WeakTypeTag, E: c.WeakTypeTag](dummy: c.Tree): c.Expr[Unmarshaller[V, E]] = {
    val EnumEntry = weakTypeOf[E]
    assertValueEnumEntry(EnumEntry,
                         s"To materialize value enum format ${EnumEntry.typeSymbol} must subclass enumeratum.values.ValueEnumEntry")

    val ValueType = weakTypeOf[V]
    c.Expr[Unmarshaller[V, E]](q"${_this}.valueEnumUnmarshaller[$ValueType, $EnumEntry](${companion(EnumEntry)})")
  }
}
