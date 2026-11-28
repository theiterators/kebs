package pl.iterators.kebs.core.macros.internal

import pl.iterators.kebs.core.instances.InstanceConverter
import pl.iterators.kebs.core.macros.ValueClassLike

import scala.reflect.macros.TypecheckException
import scala.util.control.NonFatal

private[kebs] trait JsonDerivationMacrosScala2 extends JsonDerivationMacrosImpl { this: hearth.MacroCommonsScala2 =>
  import c.universe._

  override protected def withFreshSearch[A](search: => A): A = FreshSearch(c)(search)

  override protected def methodsNamed[A: Type](names: Set[String]): Ignored =
    new Ignored(withCompanion(Type[A].tpe.asInstanceOf[c.Type]).flatMap { tpe =>
      names.toList.flatMap(name => tpe.member(TermName(name)).alternatives)
    })

  override protected def implicitsOf[A: Type]: Ignored =
    new Ignored(withCompanion(Type[A].tpe.asInstanceOf[c.Type]).flatMap { tpe =>
      tpe.baseClasses.flatMap(_.info.decls.filter(member => member.isTerm && member.isImplicit))
    }.distinct)

  private def withCompanion(tpe: c.Type): List[c.Type] = {
    val companion = if (tpe.typeSymbol.isModuleClass) NoSymbol else tpe.typeSymbol.companion
    tpe :: (if (companion.isModule) List(companion.typeSignature) else Nil)
  }

  override protected def searchIgnoring[A: Type](ignored: Ignored): SummoningResult[A] =
    try {
      val excluded = ignored.symbols.asInstanceOf[List[Symbol]]
      val instance = c.inferImplicitValueIgnoring(Type[A].tpe.asInstanceOf[c.Type], silent = true)(excluded*)
      if (instance.isEmpty) SummoningResult.NotFound(Type[A]) else SummoningResult.Found(c.Expr[A](instance).asInstanceOf[Expr[A]])
    } catch {
      case error: TypecheckException if error.getMessage.startsWith("ambiguous implicit values:")   => SummoningResult.Ambiguous(Type[A])
      case error: TypecheckException if error.getMessage.startsWith("diverging implicit expansion") => SummoningResult.Diverging(Type[A])
      case _: TypecheckException                                                                    => SummoningResult.NotFound(Type[A])
    }

  override protected def compilerType[A: Type]: AnyRef = Type[A].tpe

  override protected def optionItemOf[A: Type]: Option[??] = Type[A].tpe.asInstanceOf[c.Type].dealias match {
    case TypeRef(_, symbol, List(item)) if symbol == definitions.OptionClass => Some(item.asInstanceOf[UntypedType].as_??)
    case _                                                                   => None
  }

  // hearth refers to them by their symbols, which is not enough inside classes
  override protected def withPath[F: Type, A](code: Expr[A]): Expr[A] = {
    val (prefix, module) = Type[F].tpe.asInstanceOf[c.Type].dealias match {
      case TypeRef(pre, symbol, _) if symbol.isModuleClass => (pre, symbol.asClass.module)
      case TypeRef(pre, symbol, _)                         => (pre, symbol.companion)
      case SingleType(pre, symbol)                         => (pre, symbol)
      case _                                               => (NoPrefix, NoSymbol)
    }
    if (prefix == NoPrefix || module == NoSymbol || module.isStatic) code
    else {
      def path     = c.internal.gen.mkAttributedRef(fromOutside(prefix), module)
      val withPath = new Transformer {
        override def transform(tree: Tree): Tree = tree match {
          case Ident(_) if tree.symbol == module     => path
          case Select(_, _) if tree.symbol == module => path
          case _                                     => super.transform(tree)
        }
      }
      c.Expr[Any](withPath.transform(code.tree)).asInstanceOf[Expr[A]]
    }
  }

  // a case of a sealed hierarchy is known as a member of the object it is in: Domain.this.Case
  private def fromOutside(prefix: c.Type): c.Type = prefix match {
    case ThisType(owner) if owner.isModuleClass && !owner.isStatic =>
      val outer = if (owner.owner.isClass) fromOutside(c.internal.thisType(owner.owner)) else NoPrefix
      c.internal.singleType(outer, owner.asClass.module)
    case _ => prefix
  }

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
      .filter(owner => owner.isTerm && isTyped(owner))
      .map(owner => if (owner.owner.isClass && owner.asTerm.getter != NoSymbol) owner.asTerm.getter else owner)
      .find(owner => owner.isImplicit && isOf(owner, codec))
    hidden.map(owner => TermName(owner.name.toString.trim))
  }

  private var hidden: Option[Symbol] = None

  private def isOf(owner: Symbol, codec: c.Type): Boolean =
    resultType(owner).exists(_.baseType(codec.typeSymbol).toString == codec.toString)

  override protected def mentionsHidden[A](expr: Expr[A]): Boolean = hidden.exists(symbol => expr.tree.exists(_.symbol == symbol))

  override protected def isHidden[A: Type]: Boolean = hidden.exists(isOf(_, Type[A].tpe.asInstanceOf[c.Type]))

  // Asking for a type that is still being inferred is an error the compiler remembers: it would not find the implicit any more.
  private def isTyped(owner: Symbol): Boolean = owner.asInstanceOf[scala.reflect.internal.Symbols#Symbol].hasCompleteInfo

  private def resultType(owner: Symbol): Option[c.Type] =
    try Some(owner.typeSignature.finalResultType)
    catch { case NonFatal(_) => None }

  override protected def valueClassOf[A: Type]: Option[Wrapping] = wrapping[A](typeOf[ValueClassLike[Any, Any]])
  override protected def converterOf[A: Type]: Option[Wrapping]  = wrapping[A](typeOf[InstanceConverter[Any, Any]])

  private def wrapping[A: Type](typeClass: c.Type): Option[Wrapping] = {
    val wanted   = appliedType(typeClass.typeConstructor, Type[A].tpe.asInstanceOf[c.Type], WildcardType)
    val instance = JsonDerivationState.searching(expansionOwner)(withFreshSearch(c.inferImplicitValue(wanted, silent = true)))
    if (instance.isEmpty) None
    else {
      val found = instance.tpe.baseType(typeClass.typeSymbol)
      found.typeArgs.lift(1).map(base => new Wrapping(instance, base.dealias.asInstanceOf[UntypedType].as_??))
    }
  }

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
    val configName = TermName(c.freshName("kebsConfig"))
    needed.instances match {
      // nothing else needs the root's instance, so none is defined: the class costs more to compile than the codec. The
      // level is derived here, in the scope it would have, where the searches it needs were made already.
      case List(root) if !needed.refersToRoot && result.isEmpty =>
        import root.tpe.Underlying as X
        val level = ownLevel(root, q"$configName").getOrElse(define(root, q"$configName", Type[X].tpe.asInstanceOf[c.Type])._2)
        q"{ val $configName: _root_.pl.iterators.kebs.core.json.JsonConfig = $config; $level }"
      case _ => withClass(config, configName, needed, rootName)(define)(result)
    }
  }

  private def withClass(config: Tree, configName: TermName, needed: Needed, rootName: Option[TermName])(
      define: (LocalInstance, Tree, c.Type) => (Tree, Tree)
  )(result: Option[Tree]): Tree = {
    val codecs    = TypeName(c.freshName("KebsCodecs"))
    val instances = needed.instances.zipWithIndex.map { case (instance, index) =>
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

  // the code of a local instance's level, given the config
  protected def ownLevel(instance: LocalInstance, config: Tree): Option[Tree] = None

  // re-typed, so that the owners of the local codecs are fixed before lambdalift sees the references between them
  protected def retyped[A: c.WeakTypeTag](expr: Expr[A]): c.Expr[A] = c.Expr[A](c.untypecheck(expr.tree))
}
