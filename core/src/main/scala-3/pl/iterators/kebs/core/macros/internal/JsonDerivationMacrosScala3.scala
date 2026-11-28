package pl.iterators.kebs.core.macros.internal

import pl.iterators.kebs.core.json.JsonConfig

import scala.deriving.Mirror
import scala.quoted.*

private[kebs] trait JsonDerivationMacrosScala3 extends JsonDerivationMacrosImpl { this: hearth.MacroCommonsScala3 =>

  override protected def constructViaMirror[F: Type](arguments: List[Expr_??]): Option[Expr[F]] = {
    withQuotes {
      scala.quoted.Expr.summon[Mirror.ProductOf[F]].map { mirror =>
        val values = Varargs(arguments.map(_.value.asInstanceOf[scala.quoted.Expr[Any]]))
        '{ $mirror.fromProduct(Tuple.fromArray(Array[Any]($values*))).asInstanceOf[F] }
      }
    }
  }

  override protected def methodsNamed[A: Type](names: Set[String]): Ignored = {
    import quotes.reflect.*
    new Ignored(Members.declared(using quotes)(TypeRepr.of(using summon[Type[A]]))(member => names(member.name)))
  }

  override protected def implicitsOf[A: Type]: Ignored = {
    import quotes.reflect.*
    new Ignored(Members.declared(using quotes)(TypeRepr.of(using summon[Type[A]])) { member =>
      member.flags.is(Flags.Implicit) || member.flags.is(Flags.Given)
    })
  }

  override protected def searchIgnoring[A: Type](ignored: Ignored): SummoningResult[A] = {
    import quotes.reflect.*
    Implicits.searchIgnoring(TypeRepr.of(using summon[Type[A]]))(ignored.symbols.asInstanceOf[List[Symbol]]*) match {
      case found: ImplicitSearchSuccess => SummoningResult.Found(found.tree.asExprOf[A])
      case _: AmbiguousImplicits        => SummoningResult.Ambiguous(summon[Type[A]])
      case _: DivergingImplicit         => SummoningResult.Diverging(summon[Type[A]])
      case _                            => SummoningResult.NotFound(summon[Type[A]])
    }
  }

  override protected def compilerType[A: Type]: AnyRef = {
    import quotes.reflect.*
    TypeRepr.of(using summon[Type[A]]).asInstanceOf[AnyRef]
  }

  override protected def optionItemOf[A: Type]: Option[??] = {
    import quotes.reflect.*
    TypeRepr.of(using summon[Type[A]]).dealias match {
      case AppliedType(option, List(item)) if option.typeSymbol == Symbol.requiredClass("scala.Option") =>
        Some(item.asInstanceOf[UntypedType].as_??)
      case _ => None
    }
  }

  override protected def valueClassOf[A: Type]: Option[Wrapping] = wrapping[A]("pl.iterators.kebs.core.macros.ValueClassLike")
  override protected def converterOf[A: Type]: Option[Wrapping]  = wrapping[A]("pl.iterators.kebs.core.instances.InstanceConverter")

  private def wrapping[A: Type](typeClass: String): Option[Wrapping] = {
    import quotes.reflect.*
    val symbol = Symbol.requiredClass(typeClass)
    val wanted = symbol.typeRef.appliedTo(List(TypeRepr.of(using summon[Type[A]]), TypeBounds.empty))
    JsonDerivationState.searching(expansionOwner)(Implicits.search(wanted)) match {
      case found: ImplicitSearchSuccess =>
        found.tree.tpe.widen.baseType(symbol).typeArgs.lift(1).map { base =>
          new Wrapping(found.tree.asInstanceOf[UntypedExpr], base.dealias.asInstanceOf[UntypedType].as_??)
        }
      case ambiguous: AmbiguousImplicits =>
        Environment.reportErrorAndAbort(s"Ambiguous instances of ${wanted.show}: ${ambiguous.explanation}")
      case diverging: DivergingImplicit => Environment.reportErrorAndAbort(s"Diverging search for ${wanted.show}")
      case _                            => None
    }
  }

  override protected def placeholderKeys[A](expr: Expr[A]): Set[String] = {
    import quotes.reflect.*
    val placeholder = Symbol.requiredMethod("pl.iterators.kebs.core.macros.internal.JsonPlaceholder.apply")
    val collect     = new TreeAccumulator[Set[String]] {
      def foldTree(keys: Set[String], tree: Tree)(owner: Symbol): Set[String] = tree match {
        case Apply(TypeApply(fun, _), List(key)) if fun.symbol == placeholder => keys ++ key.asExprOf[String].value
        case _                                                                => foldOverTree(keys, tree)(owner)
      }
    }
    collect.foldTree(Set.empty, expr.asTerm)(Symbol.spliceOwner)
  }

  // an inferred type argument of a method that accepts a supertype is only visible here
  override protected def madeBy[A](methods: Ignored, expr: Expr[A]): List[??] =
    if (methods.isEmpty) Nil
    else {
      import quotes.reflect.*
      val symbols = methods.symbols.toSet[Any]
      val collect = new TreeAccumulator[List[UntypedType]] {
        def foldTree(types: List[UntypedType], tree: Tree)(owner: Symbol): List[UntypedType] = tree match {
          case call @ Apply(_, _) if symbols(call.symbol) =>
            foldOverTree(call.tpe.widen.dealias.typeArgs.headOption.toList.map(_.asInstanceOf[UntypedType]) ++ types, tree)(owner)
          case _ => foldOverTree(types, tree)(owner)
        }
      }
      collect.foldTree(Nil, expr.asTerm)(Symbol.spliceOwner).distinct.map(_.as_??)
    }

  override protected def expansionOwner: AnyRef = quotes.reflect.Symbol.spliceOwner.asInstanceOf[AnyRef]

  override protected def isExpandedInside(owner: AnyRef): Boolean =
    Iterator.iterate(quotes.reflect.Symbol.spliceOwner)(_.maybeOwner).takeWhile(!_.isNoSymbol).contains(owner)

  // for LocalInstances, whose Quotes have another path
  protected def untyped(needed: Needed): LocalInstances.Needed = {
    import quotes.reflect.*
    LocalInstances.Needed(
      needed.instances.map { instance =>
        import instance.tpe.Underlying as X
        LocalInstances.Untyped(instance.typeClass, TypeRepr.of(using summon[Type[X]]), instance.isCollection)
      },
      needed.rootIsImplicit,
      needed.refersToRoot
    )
  }
}

// Symbol.methodMember lists every member of the class, and hearth would describe each one
private[kebs] object Members {
  // the term members of a type and its companion, inherited ones included
  def declared(using q: Quotes)(tpe: q.reflect.TypeRepr)(wanted: q.reflect.Symbol => Boolean): List[q.reflect.Symbol] = {
    import q.reflect.*
    val owner     = tpe.dealias.typeSymbol
    val companion = if (owner.flags.is(Flags.Module)) Nil else List(owner.companionModule.moduleClass).filterNot(_.isNoSymbol)
    (owner :: companion).flatMap { cls =>
      cls.typeRef.baseClasses.flatMap(_.declarations.filter(member => member.isTerm && wanted(member)))
    }.distinct
  }
}

private[kebs] object LocalInstances {
  final class Untyped(val typeClass: String, val typeRepr: Any, val isCollection: Boolean)

  final class Needed(val instances: List[Untyped], val rootIsImplicit: Boolean, val refersToRoot: Boolean)

  final class Definition[C](val codec: Expr[C])(using val tpe: Type[C]) {
    def creator(using Quotes): Expr[() => Any] = '{ () => $codec }
  }

  trait Define {
    def apply[X: Type](instance: Untyped, config: Expr[JsonConfig])(using Quotes): Definition[?]
    // the code of the level itself, instead of the call of its macro
    def ownLevel[X: Type](instance: Untyped, config: Expr[JsonConfig])(using Quotes): Option[Definition[?]] = None
  }

  // { lazy val config; lazy val codecs = LocalCodecs(() => level, ...); implicit def codec: Codec[X] = codecs.get(0); ... ; result }
  // Not lazy vals: they are parameters of the closures that use them, of which there may be more than a method can have.
  def block[R: Type](config: Expr[JsonConfig], needed: Needed)(define: Define)(result: Option[Expr[R]])(using Quotes): Expr[R] = {
    import quotes.reflect.*
    val owner = Symbol.spliceOwner
    needed.instances match {
      // nothing else needs the root's instance, so none is defined, and its level is derived here, in the scope it would
      // have, where the searches it needs were made already
      case List(root) if !needed.refersToRoot && result.isEmpty =>
        val configSymbol = Symbol.newVal(owner, "kebsConfig", TypeRepr.of[JsonConfig], Flags.EmptyFlags, Symbol.noSymbol)
        val configValue  = ValDef(configSymbol, Some(config.asTerm.changeOwner(configSymbol)))
        val configRef    = Ref(configSymbol).asExprOf[JsonConfig]
        val definition   = root.typeRepr.asInstanceOf[TypeRepr].asType match {
          case '[x] => define.ownLevel[x](root, configRef).getOrElse(define[x](root, configRef))
        }
        Block(List(configValue), definition.codec.asTerm).asExprOf[R]
      case _ => withLazyVals[R](config, needed)(define)(result)
    }
  }

  private def withLazyVals[R: Type](config: Expr[JsonConfig], needed: Needed)(define: Define)(result: Option[Expr[R]])(using
      Quotes
  ): Expr[R] = {
    import quotes.reflect.*
    val owner        = Symbol.spliceOwner
    val configSymbol = Symbol.newVal(owner, "kebsConfig", TypeRepr.of[JsonConfig], Flags.Lazy, Symbol.noSymbol)
    val codecsSymbol = Symbol.newVal(owner, "kebsCodecs", TypeRepr.of[LocalCodecs], Flags.Lazy, Symbol.noSymbol)
    val configValue  = ValDef(configSymbol, Some(config.asTerm.changeOwner(configSymbol)))
    val definitions  = needed.instances.map { instance =>
      instance.typeRepr.asInstanceOf[TypeRepr].asType match {
        case '[x] => define[x](instance, Ref(configSymbol).asExprOf[JsonConfig])
      }
    }
    val instances = definitions.zipWithIndex.map { (definition, index) =>
      instanceAt(owner, definition, index, isImplicit = index > 0 || needed.rootIsImplicit, Ref(codecsSymbol).asExprOf[LocalCodecs])
    }
    val creators    = Expr.ofList(definitions.map(_.creator))
    val codecsValue = ValDef(codecsSymbol, Some('{ LocalCodecs($creators) }.asTerm.changeOwner(codecsSymbol)))
    Block(configValue :: codecsValue :: instances, result.fold[Term](Ref(instances.head.symbol))(_.asTerm)).asExprOf[R]
  }

  private def instanceAt[C](using
      Quotes
  )(
      owner: quotes.reflect.Symbol,
      definition: Definition[C],
      index: Int,
      isImplicit: Boolean,
      codecs: Expr[LocalCodecs]
  ): quotes.reflect.DefDef = {
    import quotes.reflect.*
    given Type[C] = definition.tpe
    val flags     = if (isImplicit) Flags.Implicit else Flags.EmptyFlags
    val symbol    = Symbol.newMethod(owner, s"kebsCodec$$$index", ByNameType(TypeRepr.of[C]), flags, Symbol.noSymbol)
    DefDef(symbol, _ => Some('{ $codecs.get[C](${ Expr(index) }) }.asTerm.changeOwner(symbol)))
  }
}
