package pl.iterators.kebs.core.macros.internal

import scala.util.control.NonFatal

private[kebs] trait JsonDerivationMacrosScala2 extends JsonDerivationMacrosImpl { this: hearth.MacroCommonsScala2 =>
  import c.universe._

  override protected def withFreshSearch[A](search: => A): A = FreshSearch(c)(search)

  // Without a fresh search, an outer one can make a search diverge, and an instance would be missed without a word. Failing
  // here would be taken for the lack of an instance too, so the search goes on with something that does not compile.
  protected def unlessInsideSearch(ownSearches: Int, result: c.Type)(derived: => Tree): Tree = {
    val openSearches = c match {
      case whitebox: scala.reflect.macros.whitebox.Context => whitebox.enclosingImplicits.size
      case _                                               => 0
    }
    if (openSearches > ownSearches && !FreshSearch.isAvailable(c))
      q"_root_.pl.iterators.kebs.core.macros.internal.JsonPlaceholder.unavailable[$result]"
    else derived
  }

  override protected def expansionOwner: AnyRef = c.internal.enclosingOwner

  override protected def isExpandedInside(owner: AnyRef): Boolean = enclosingOwners.contains(owner)

  private def enclosingOwners: Iterator[Symbol] = Iterator.iterate(c.internal.enclosingOwner)(_.owner).takeWhile(_ != NoSymbol)

  // the implicit of this type that is being defined by this expansion; an instance of the same name hides it. Compared as
  // printed, because inside a method its type parameters are other symbols than in its signature.
  protected def enclosingImplicit(codec: c.Type): Option[TermName] = {
    hidden = enclosingOwners
      .takeWhile(!_.isClass)
      .filter(_.isTerm)
      .map(owner => if (owner.asTerm.getter != NoSymbol) owner.asTerm.getter else owner)
      .find(owner => owner.isImplicit && isOf(owner, codec))
    hidden.map(owner => TermName(owner.name.toString.trim))
  }

  private var hidden: Option[Symbol] = None

  private def isOf(owner: Symbol, codec: c.Type): Boolean =
    resultType(owner).exists(_.baseType(codec.typeSymbol).toString == codec.toString)

  override protected def mentionsHidden[A](expr: Expr[A]): Boolean = hidden.exists(symbol => expr.tree.exists(_.symbol == symbol))

  override protected def isHidden[A: Type]: Boolean = hidden.exists(isOf(_, Type[A].tpe.asInstanceOf[c.Type]))

  // an inferred result type is not known yet
  private def resultType(owner: Symbol): Option[c.Type] =
    try Some(owner.typeSignature.finalResultType)
    catch { case NonFatal(_) => None }

  override protected def placeholderKeys[A](expr: Expr[A]): Set[String] = expr.tree.collect {
    case Apply(TypeApply(fun, _), List(Literal(Constant(key: String)))) if fun.symbol.fullName == placeholderName => key
  }.toSet

  private val placeholderName = "pl.iterators.kebs.core.macros.internal.JsonPlaceholder.apply"

  // { class Codecs { lazy val config; implicit lazy val codec: Codec[X] = level; ... }; (new Codecs).codec }
  // A class: local lazy vals are parameters of the closures that use them, of which there may be more than a method can
  // have, and cannot be used in the arguments of a super constructor.
  // define: the type and the definition of an instance, given the config and X
  protected def localInstances(config: Tree, needed: Needed, rootName: Option[TermName])(
      define: (LocalInstance, Tree, c.Type) => (Tree, Tree)
  )(result: Option[Tree]): Tree = {
    val codecs     = TypeName(c.freshName("KebsCodecs"))
    val configName = TermName(c.freshName("kebsConfig"))
    val instances  = needed.instances.zipWithIndex.map { case (instance, index) =>
      import instance.tpe.Underlying as X
      val (codec, level) = define(instance, q"$configName", Type[X].tpe.asInstanceOf[c.Type])
      val name           = rootName.filter(_ => index == 0).getOrElse(TermName(c.freshName("kebsCodec")))
      val mods           = if (index == 0 && !needed.rootIsImplicit) Modifiers(Flag.LAZY) else Modifiers(Flag.LAZY | Flag.IMPLICIT)
      ValDef(mods, name, codec, level)
    }
    val (results, member) = result match {
      case Some(tree) =>
        val name = TermName(c.freshName("kebsResult"))
        (List(q"lazy val $name = $tree"), name)
      case None => (Nil, instances.head.name)
    }
    q"""{
      final class $codecs {
        lazy val $configName: _root_.pl.iterators.kebs.core.json.JsonConfig = $config
        ..$instances
        ..$results
      }
      (new $codecs).$member
    }"""
  }

  // re-typed, so that the owners of the local codecs are fixed before lambdalift sees the references between them
  protected def retyped[A: c.WeakTypeTag](expr: Expr[A]): c.Expr[A] = c.Expr[A](c.untypecheck(expr.tree))
}
