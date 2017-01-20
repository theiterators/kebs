package pl.iterators.kebs.json.enums.macros

import pl.iterators.kebs.macros.EnumMacroUtils
import play.api.libs.json._

import scala.reflect.macros._

class KebsPlayEnumMacros(override val c: whitebox.Context) extends EnumMacroUtils {
  import c.universe._

  private def materializeReads[E](enumMapComap: EnumMapComap[E, String]) = {
    val E = enumMapComap.E
    assertEnumEntry(E, s"To materialize enum Reads ${E.typeSymbol} must subclass enumeratum.EnumEntry")

    val name           = TermName("name")
    val maybeEnumEntry = enumMapComap.comapOption(name)

    val reads =
      q"""(json: _root_.play.api.libs.json.JsValue) => json match {
          case _root_.play.api.libs.json.JsString($name) => $maybeEnumEntry.map(JsSuccess(_)).getOrElse(
            _root_.play.api.libs.json.JsError("error.expected.validenumvalue"))
          case _ => _root_.play.api.libs.json.JsError("error.expected.enumstring")
        }
       """

    c.Expr[Reads[E]](q"_root_.play.api.libs.json.Reads[$E]($reads)")
  }
  private def materializeWrites[E](enumMapComap: EnumMapComap[E, String]) = {
    val E      = enumMapComap.E
    val obj    = TermName("obj")
    val writes = q"($obj: $E) => _root_.play.api.libs.json.JsString(${enumMapComap.map(obj)})"

    c.Expr[Writes[E]](q"_root_.play.api.libs.json.Writes[$E]($writes)")
  }

  def materializeEnumReads[E: c.WeakTypeTag]: c.Expr[Reads[E]]                           = materializeReads[E](new EntryNameInsensitive)
  def materializeEnumUppercaseReads[E: c.WeakTypeTag]: c.Expr[Reads[E]]                  = materializeReads[E](new Uppercase)
  def materializeEnumLowercaseReads[E: c.WeakTypeTag]: c.Expr[Reads[E]]                  = materializeReads[E](new Lowercase)
  def materializeEnumWrites[E: c.WeakTypeTag](dummy: c.Tree): c.Expr[Writes[E]]          = materializeWrites[E](new EntryNameInsensitive)
  def materializeEnumUppercaseWrites[E: c.WeakTypeTag](dummy: c.Tree): c.Expr[Writes[E]] = materializeWrites[E](new Uppercase)
  def materializeEnumLowercaseWrites[E: c.WeakTypeTag](dummy: c.Tree): c.Expr[Writes[E]] = materializeWrites[E](new Lowercase)
}
