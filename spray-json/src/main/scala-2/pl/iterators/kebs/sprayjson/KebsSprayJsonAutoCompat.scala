package pl.iterators.kebs.sprayjson

import pl.iterators.kebs.core.instances.InstanceConverter
import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.macros.ValueClassLike
import pl.iterators.kebs.core.macros.internal.{EntryPointRole, JsonDerivationMacrosScala2}
import pl.iterators.kebs.sprayjson.internal.SprayFormatMacrosImpl
import spray.json.{JsonFormat, JsonReader, JsonWriter, RootJsonFormat, RootJsonReader, RootJsonWriter}

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

private[sprayjson] trait KebsSprayJsonAutoCompat { self: KebsSprayJsonSemiauto =>
  implicit def kebsAutoJsonFormat[T]: RootJsonFormat[T] = macro KebsSprayJsonMacros.autoProduct[T]
  implicit def kebsAutoJsonFormatSealed[T]: RootJsonFormat[T] = macro KebsSprayJsonMacros.autoSealed[T]
}

// always derives the type itself, with the protocol's configuration
private[sprayjson] trait KebsSprayJsonDeriveCompat { self: KebsSprayJsonSemiauto =>
  def deriveFormat[T]: RootJsonFormat[T] = macro KebsSprayJsonMacros.explicitAny[T]

  @deprecated("use deriveFormat", "3.0.0")
  def jsonFormatN[T]: RootJsonFormat[T] = macro KebsSprayJsonMacros.explicitProduct[T]
  @deprecated("use deriveFormat", "3.0.0")
  def jsonFormatSealed[T]: RootJsonFormat[T] = macro KebsSprayJsonMacros.explicitSealed[T]
  @deprecated("use deriveFormat, which handles recursive types", "3.0.0")
  final def jsonFormatRec[T]: RootJsonFormat[T] = macro KebsSprayJsonMacros.explicitProduct[T]
}

final private[sprayjson] class KebsSprayJsonMacros(val c: whitebox.Context)
    extends hearth.MacroCommonsScala2
    with JsonDerivationMacrosScala2
    with SprayFormatMacrosImpl {
  import c.universe.{Type => UType, _}

  private def isLookingFor(typeClasses: UType*): Boolean =
    c.enclosingImplicits.headOption.exists(search => typeClasses.exists(_.typeSymbol == search.pt.typeSymbol))

  private def rootWanted: Boolean =
    isLookingFor(typeOf[RootJsonFormat[Any]], typeOf[RootJsonReader[Any]], typeOf[RootJsonWriter[Any]])

  // the product entry is more specific than jsonFlatFormat and jsonConversionFormat2, so it steps aside when a
  // JsonFormat/Reader/Writer of their types is wanted; a RootJsonFormat request derives an object
  private def flatPreferred(T: UType): Boolean =
    isLookingFor(typeOf[JsonFormat[Any]], typeOf[JsonReader[Any]], typeOf[JsonWriter[Any]]) &&
      (isImplicit(appliedType(typeOf[InstanceConverter[Any, Any]].typeConstructor, T, WildcardType)) || isValueClass(T))

  private def isValueClass(T: UType): Boolean =
    T.decls.collect { case m: MethodSymbol if m.isCaseAccessor => m }.toList match {
      case single :: Nil =>
        isImplicit(appliedType(typeOf[ValueClassLike[Any, Any]].typeConstructor, T, single.typeSignatureIn(T).finalResultType))
      case _ => false
    }

  private def isImplicit(tpe: UType): Boolean = c.inferImplicitValue(tpe, silent = true) != EmptyTree

  private def abort(reason: String): Nothing = c.abort(c.enclosingPosition, reason)

  private def isCaseClass(T: UType): Boolean = T.typeSymbol.isClass && T.typeSymbol.asClass.isCaseClass
  private def isSealed(T: UType): Boolean    = T.typeSymbol.isClass && T.typeSymbol.asClass.isSealed && !isCaseClass(T)

  private def config: Expr[JsonConfig] = c.Expr[JsonConfig](q"${c.prefix}.kebsJsonConfig")

  def autoProduct[T: c.WeakTypeTag]: c.Expr[RootJsonFormat[T]] = implicitEntry {
    val T = weakTypeOf[T]
    if (!isCaseClass(T)) abort(s"$T is not a case class")
    // spray formats the standard library itself
    if (!isDerived[T](config)) abort(s"$T is not derived here")
    if (flatPreferred(T)) abort("Flat format preferred")
    auto[T](stepsAsideForExisting = !rootWanted)
  }

  // always steps aside when a format exists: kebs' value enum formats are neither more nor less specific than this one
  def autoSealed[T: c.WeakTypeTag]: c.Expr[RootJsonFormat[T]] = implicitEntry {
    val T = weakTypeOf[T]
    if (!isSealed(T)) abort(s"$T is not a sealed hierarchy")
    if (!isDerived[T](config)) abort(s"$T is not derived here")
    auto[T](stepsAsideForExisting = true)
  }

  private def auto[T: c.WeakTypeTag](stepsAsideForExisting: Boolean): c.Expr[RootJsonFormat[T]] =
    formatRole[T](stepsAsideForExisting) match {
      case EntryPointRole.StepAside => abort(s"${weakTypeOf[T]} is not derived here")
      case EntryPointRole.Record    => recordedFormat[T](config)
      case EntryPointRole.Derive    =>
        block[T](ownSearches = 1, neededFormats[T](config, preferExisting = false), rootName = None)(None)
      case EntryPointRole.UseExisting =>
        val existing = q"$localFormats.existing[${weakTypeOf[T]}]"
        block[T](ownSearches = 1, neededBesideExisting[T](config), rootName = None)(Some(existing))
    }

  def explicitProduct[T: c.WeakTypeTag]: c.Expr[RootJsonFormat[T]] = {
    if (!isCaseClass(weakTypeOf[T])) abort(s"${weakTypeOf[T]} is not a case class")
    explicitly[T]
  }

  def explicitAny[T: c.WeakTypeTag]: c.Expr[RootJsonFormat[T]] = {
    val T = weakTypeOf[T]
    if (!isCaseClass(T) && !isSealed(T)) abort(s"$T is not a case class or a sealed hierarchy")
    explicitly[T]
  }

  def explicitSealed[T: c.WeakTypeTag]: c.Expr[RootJsonFormat[T]] = {
    if (!isSealed(weakTypeOf[T])) abort(s"${weakTypeOf[T]} is not a sealed hierarchy")
    explicitly[T]
  }

  private def explicitly[T: c.WeakTypeTag]: c.Expr[RootJsonFormat[T]] = explicitEntry {
    val enclosing = enclosingImplicit(weakTypeOf[JsonFormat[T]])
    val needed    = neededFormats[T](config, preferExisting = enclosing.isEmpty, hidesExisting = enclosing.nonEmpty)
    block[T](ownSearches = 0, needed, enclosing)(None)
  }

  private def localFormats: Tree = q"_root_.pl.iterators.kebs.sprayjson.internal.SprayLocalFormats"

  private def block[T: c.WeakTypeTag](ownSearches: Int, needed: => Needed, rootName: Option[TermName])(
      result: Option[Tree]
  ): c.Expr[RootJsonFormat[T]] =
    c.Expr[RootJsonFormat[T]](unlessInsideSearch(ownSearches, weakTypeOf[RootJsonFormat[T]]) {
      localInstances(config.tree, needed, rootName) { (_, config, tpe) =>
        (tq"_root_.spray.json.RootJsonFormat[$tpe]", q"$localFormats.level[$tpe]($config)")
      }(result)
    })

  def level[T: c.WeakTypeTag](config: c.Expr[JsonConfig]): c.Expr[RootJsonFormat[T]] = retyped(formatLevel[T](config))
  def existing[T: c.WeakTypeTag]: c.Expr[RootJsonFormat[T]]                          = explicitEntry(retyped(existingFormat[T]))
}
