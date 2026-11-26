package pl.iterators.kebs.playjson

import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.macros.internal.{EntryPointRole, JsonDerivationMacrosScala2}
import pl.iterators.kebs.playjson.internal.PlayJsonMacrosImpl
import play.api.libs.json.{OFormat, OWrites, Reads, Writes}

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

// a parent of KebsPlayJson, so that its flat and InstanceConverter instances win by owner
private[playjson] trait KebsPlayJsonAutoCompat { self: KebsPlayJsonSemiauto =>
  implicit def kebsAutoReads[T]: Reads[T] = macro KebsPlayJsonMacros.autoReads[T]
  implicit def kebsAutoReadsSealed[T]: Reads[T] = macro KebsPlayJsonMacros.autoReadsSealed[T]
  implicit def kebsAutoWrites[T]: OWrites[T] = macro KebsPlayJsonMacros.autoWrites[T]
  implicit def kebsAutoWritesSealed[T]: OWrites[T] = macro KebsPlayJsonMacros.autoWritesSealed[T]
}

// always derive the type itself, with the protocol's configuration
private[playjson] trait KebsPlayJsonDeriveCompat { self: KebsPlayJsonSemiauto =>
  def deriveReads[T]: Reads[T] = macro KebsPlayJsonMacros.explicitReads[T]
  def deriveWrites[T]: OWrites[T] = macro KebsPlayJsonMacros.explicitWrites[T]
  def deriveFormat[T]: OFormat[T] = macro KebsPlayJsonMacros.explicitFormat[T]
}

final private[playjson] class KebsPlayJsonMacros(val c: whitebox.Context)
    extends hearth.MacroCommonsScala2
    with JsonDerivationMacrosScala2
    with PlayJsonMacrosImpl {
  import c.universe.{Type => UType, _}

  private def abort(reason: String): Nothing = c.abort(c.enclosingPosition, reason)

  private def isCaseClass(T: UType): Boolean = T.typeSymbol.isClass && T.typeSymbol.asClass.isCaseClass
  private def isSealed(T: UType): Boolean    = T.typeSymbol.isClass && T.typeSymbol.asClass.isSealed && !isCaseClass(T)

  private def config: Expr[JsonConfig] = c.Expr[JsonConfig](q"${c.prefix}.kebsJsonConfig")

  private def requireProduct[T: c.WeakTypeTag](): Unit = {
    val T = weakTypeOf[T]
    if (!isCaseClass(T)) abort(s"$T is not a case class")
    // play's Writes is invariant, so kebs' enum instances do not apply to a case object's own type: deriving `{}` there
    // would replace a compile error with wrong JSON
    if (T.typeSymbol.isModuleClass) abort(s"$T is a case object; it is derived only as a case")
    requireOwn[T]()
  }

  private def requireSealed[T: c.WeakTypeTag](): Unit = {
    if (!isSealed(weakTypeOf[T])) abort(s"${weakTypeOf[T]} is not a sealed hierarchy")
    requireOwn[T]()
  }

  // play formats the standard library itself
  private def requireOwn[T: c.WeakTypeTag](): Unit = if (isStandardLibrary[T]) abort(s"${weakTypeOf[T]} is not derived here")

  private def requireDerivable[T: c.WeakTypeTag](): Unit = {
    val T = weakTypeOf[T]
    if (!isCaseClass(T) && !isSealed(T)) abort(s"$T is not a case class or a sealed hierarchy")
  }

  def autoReads[T: c.WeakTypeTag]: c.Expr[Reads[T]]          = implicitEntry { requireProduct[T](); reads[T] }
  def autoReadsSealed[T: c.WeakTypeTag]: c.Expr[Reads[T]]    = implicitEntry { requireSealed[T](); reads[T] }
  def autoWrites[T: c.WeakTypeTag]: c.Expr[OWrites[T]]       = implicitEntry { requireProduct[T](); writes[T] }
  def autoWritesSealed[T: c.WeakTypeTag]: c.Expr[OWrites[T]] = implicitEntry { requireSealed[T](); writes[T] }

  // the implicit entries step aside when an instance exists: a user's Reads[X] is neither more nor less specific than
  // kebsAutoReads[X]
  private def reads[T: c.WeakTypeTag]: c.Expr[Reads[T]] = readsRole[T](stepsAsideForExisting = true) match {
    case EntryPointRole.Record => recordedReads[T](config)
    case EntryPointRole.Derive =>
      c.Expr[Reads[T]](block(ownSearches = 1, weakTypeOf[Reads[T]])(neededReads[T](config, preferExisting = false), rootName = None))
    case _ => abort(s"${weakTypeOf[T]} is not derived here")
  }

  private def writes[T: c.WeakTypeTag]: c.Expr[OWrites[T]] = writesRole[T] match {
    case EntryPointRole.Record => recordedWrites[T](config)
    case EntryPointRole.Derive =>
      c.Expr[OWrites[T]](block(ownSearches = 1, weakTypeOf[OWrites[T]])(neededWrites[T](config, preferExisting = false), rootName = None))
    case _ => abort(s"${weakTypeOf[T]} is not derived here")
  }

  private def explicitReadsBlock[T: c.WeakTypeTag]: Tree = {
    val enclosing = enclosingImplicit(weakTypeOf[Reads[T]])
    block(ownSearches = 0, weakTypeOf[Reads[T]])(
      neededReads[T](config, preferExisting = enclosing.isEmpty, hidesExisting = enclosing.nonEmpty),
      enclosing
    )
  }

  private def explicitWritesBlock[T: c.WeakTypeTag]: Tree = {
    val enclosing = enclosingImplicit(weakTypeOf[Writes[T]])
    block(ownSearches = 0, weakTypeOf[OWrites[T]])(
      neededWrites[T](config, preferExisting = enclosing.isEmpty, hidesExisting = enclosing.nonEmpty),
      enclosing
    )
  }

  def explicitReads[T: c.WeakTypeTag]: c.Expr[Reads[T]] = explicitEntry {
    requireDerivable[T]()
    c.Expr[Reads[T]](explicitReadsBlock[T])
  }

  def explicitWrites[T: c.WeakTypeTag]: c.Expr[OWrites[T]] = explicitEntry {
    requireDerivable[T]()
    c.Expr[OWrites[T]](explicitWritesBlock[T])
  }

  def explicitFormat[T: c.WeakTypeTag]: c.Expr[OFormat[T]] = explicitEntry {
    requireDerivable[T]()
    c.Expr[OFormat[T]](
      q"_root_.play.api.libs.json.OFormat[${weakTypeOf[T]}](${explicitReadsBlock[T]}, ${explicitWritesBlock[T]})"
    )
  }

  private def block(ownSearches: Int, result: UType)(needed: => Needed, rootName: Option[TermName]): Tree =
    unlessInsideSearch(ownSearches, result) {
      val instances = q"_root_.pl.iterators.kebs.playjson.internal.PlayLocalInstances"
      localInstances(config.tree, needed, rootName) { (instance, config, tpe) =>
        if (instance.typeClass == "Reads") (tq"_root_.play.api.libs.json.Reads[$tpe]", q"$instances.reads[$tpe]($config)")
        else if (instance.isCollection) (tq"_root_.play.api.libs.json.Writes[$tpe]", q"$instances.collectionWrites[$tpe]($config)")
        else (tq"_root_.play.api.libs.json.OWrites[$tpe]", q"$instances.writes[$tpe]($config)")
      }(None)
    }

  def oneReads[T: c.WeakTypeTag](config: c.Expr[JsonConfig]): c.Expr[Reads[T]]             = retyped(readsLevel[T](config))
  def oneWrites[T: c.WeakTypeTag](config: c.Expr[JsonConfig]): c.Expr[OWrites[T]]          = retyped(writesLevel[T](config))
  def oneCollectionWrites[T: c.WeakTypeTag](config: c.Expr[JsonConfig]): c.Expr[Writes[T]] = retyped(collectionWritesLevel[T](config))
}
