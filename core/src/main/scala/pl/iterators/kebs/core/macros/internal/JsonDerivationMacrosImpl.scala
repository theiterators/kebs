package pl.iterators.kebs.core.macros.internal

import hearth.MacroCommons
import hearth.fp.effect.MIO
import hearth.fp.instances.*
import hearth.std.StdExtensions
import pl.iterators.kebs.core.annotations.{fieldName, transientField}
import pl.iterators.kebs.core.macros.ValueClassLike

import scala.collection.Factory
import scala.collection.mutable

// shared by the JSON modules whose derivation kebs owns (spray-json, play-json); a backend generates the codec code
private[kebs] trait JsonDerivationMacrosImpl { this: MacroCommons & StdExtensions =>

  // Scala 3: a private constructor is still reachable through the Mirror's fromProduct
  protected def constructViaMirror[F: Type](arguments: List[Expr_??]): Option[Expr[F]] = None

  private var extensionsLoaded = false

  protected def loadExtensions(): Unit = if (!extensionsLoaded) {
    Environment.loadStandardExtensions() match {
      case ExtensionLoadingResult.LoaderFailed(error) =>
        Environment.reportErrorAndAbort("Failed to load hearth's standard extensions: " + error.toString)
      case ExtensionLoadingResult.SomeFailed(_, errors) =>
        Environment.reportErrorAndAbort(
          "Failed to load hearth's standard extensions: " + errors.toNonEmptyVector.map(_._2).mkString("\n")
        )
      case _ =>
    }
    extensionsLoaded = true
  }

  def isBeingDerived[T: Type]: Boolean = JsonDerivationGuard.isBeingDerived(Type[T].plainPrint)

  def isStandardLibrary[T: Type]: Boolean = {
    val name = Type.fqcn[T]
    name.startsWith("scala.") || name.startsWith("java.") || name.startsWith("(")
  }

  def summonInstance[A: Type](ignored: Seq[UntypedMethod]): Option[Expr[A]] =
    Type[A].summonExprIgnoring(ignored*) match {
      case SummoningResult.Found(found) => Some(found)
      case SummoningResult.Ambiguous(_) => Environment.reportErrorAndAbort(s"Ambiguous instances of ${Type[A].prettyPrint}")
      case _                            => None
    }

  private lazy val FieldNameType: Type[fieldName]           = Type.of[fieldName]
  private lazy val TransientFieldType: Type[transientField] = Type.of[transientField]

  final class JsonField(val param: Parameter, val index: Int) {
    val name: String            = param.name
    val tpe: ??                 = param.tpe
    val transient: Boolean      = param.hasAnnotationOfType[transientField](using TransientFieldType)
    val renamed: Option[String] = param.annotationsOfType[fieldName](using FieldNameType).headOption.map { annotation =>
      Annotations.decodedConstructorArguments(annotation).getOrElse(Nil) match {
        case List(Right(name: String)) => name
        case _                         => Environment.reportErrorAndAbort(s"@fieldName on ${param.name} needs a literal String argument")
      }
    }
    def default[A: Type]: Option[Expr[A]] = defaultOf[A](param)
  }

  abstract class JsonDerivation[Codec[_]] {
    protected def codecType[A: Type]: Type[Codec[A]]
    protected def ignoredForFields: Seq[UntypedMethod]
    protected def optionCodec[A: Type](inner: Expr[Codec[A]]): Expr[Codec[Option[A]]]
    protected def collectionCodec[Item: Type, R: Type, C: Type](
        inner: Expr[Codec[Item]],
        iterate: Expr[C] => Expr[Iterable[Item]],
        factory: Expr[Factory[Item, R]],
        build: Expr[mutable.Builder[Item, R]] => Expr[C]
    ): Expr[Codec[C]]
    protected def stringMapCodec[V: Type, P: Type, R: Type, C: Type](
        inner: Expr[Codec[V]],
        iterate: Expr[C] => Expr[Iterable[P]],
        factory: Expr[Factory[P, R]],
        key: Expr[P] => Expr[String],
        value: Expr[P] => Expr[V],
        pair: (Expr[String], Expr[V]) => Expr[P],
        build: Expr[mutable.Builder[P, R]] => Expr[C]
    ): Expr[Codec[C]]
    protected def flatCodec[T: Type, A: Type](rep: Expr[ValueClassLike[T, A]], inner: Expr[Codec[A]]): Expr[Codec[T]]
    protected def singletonCodec[T: Type](value: Expr[T]): Expr[Codec[T]]
    protected def productCodec[T: Type](product: ProductShape[T]): Expr[Codec[T]]
    // leaves of a sealed hierarchy (nested hierarchies flattened), by case name; codec(leaf) resolves each
    protected def sumCodec[T: Type](leaves: List[(String, ??)]): Expr[Codec[T]]

    private var cache: ValDefsCache = ValDefsCache.empty
    private var declared            = Set.empty[String]

    private def key[F: Type](kind: String): String = kind + ":" + Type[F].plainPrint

    final def root[T: Type]: Expr[Codec[T]] = {
      implicit val CodecT: Type[Codec[T]] = codecType[T]
      val codec                           = derived[T].getOrElse(
        Environment.reportErrorAndAbort(s"Cannot derive ${Type[T].prettyPrint}: not a case class or a sealed hierarchy")
      )
      cache.toValDefs.use(_ => codec)
    }

    final def codec[F: Type]: Expr[Codec[F]] =
      if (declared(key[F]("derived"))) cache.get0Ary[Codec[F]](key[F]("derived"))(using codecType[F]).get
      else
        existing[F]
          .orElse(structural[F])
          .orElse(flat[F])
          .orElse(derived[F])
          .getOrElse(
            Environment.reportErrorAndAbort(
              s"No ${codecType[F].prettyPrint} in scope, and kebs derives one only for case classes and sealed " +
                "hierarchies outside the standard library (a recursive type through a tuple or an Either needs an " +
                "explicit implicit lazy val)"
            )
          )

    // forward-declared before its body is built, so that recursive references resolve to it
    private def derived[F: Type]: Option[Expr[Codec[F]]] = {
      implicit val CodecF: Type[Codec[F]] = codecType[F]
      val k                               = key[F]("derived")
      if (declared(k)) cache.get0Ary[Codec[F]](k)
      else
        derivation[F].map { build =>
          val builder = ValDefBuilder.ofLazy[Codec[F]]("kebsCodec")
          cache = builder.forwardDeclare(cache, k)
          declared += k
          val body = JsonDerivationGuard.deriving(Type[F].plainPrint)(build())
          cache = builder.buildCachedWith(cache, k)(_ => body)
          cache.get0Ary[Codec[F]](k).get
        }
    }

    // standard library sums and tuples have the library's own codecs; deriving them would silently change their shape
    private def derivation[F: Type]: Option[() => Expr[Codec[F]]] =
      if (isStandardLibrary[F] && (Type.isSealed[F] || Type.fqcn[F].startsWith("scala.Tuple") || Type.fqcn[F].startsWith("(")))
        None
      else
        Expr
          .singletonOf[F]
          .map(value => () => singletonCodec[F](value))
          .orElse(CaseClass.parse[F].toOption.map(caseClass => () => productCodec[F](new ProductShape[F](caseClass))))
          .orElse(leavesOf[F].map(leaves => () => sumCodec[F](leaves)))

    // nested sealed hierarchies are flattened to their leaves (as kindlings/circe do)
    private def leavesOf[F: Type]: Option[List[(String, ??)]] =
      if (!Type.isSealed[F] || Type.isCaseClass[F]) None
      else
        Type
          .directChildrenList[F]
          .map(_.flatMap { case (name, child) =>
            import child.Underlying as C
            if (Expr.singletonOf[C].isEmpty && !Type.isCaseClass[C] && Type.isSealed[C])
              leavesOf[C].getOrElse(Nil).map { case (leafName, leaf) =>
                import leaf.Underlying as L
                leafName -> Type[L].as_??
              }
            else List(name -> Type[C].as_??)
          })
          .filter(_.nonEmpty)
          .map(_.distinctBy(_._2.Underlying.plainPrint))

    private def shared[F: Type](k: String)(codec: => Expr[Codec[F]]): Expr[Codec[F]] = {
      implicit val CodecF: Type[Codec[F]] = codecType[F]
      if (!declared(k)) {
        val value = codec
        cache = ValDefBuilder.ofLazy[Codec[F]]("kebsCodec").buildCachedWith(cache, k)(_ => value)
        declared += k
      }
      cache.get0Ary[Codec[F]](k).get
    }

    // the library's own Option/collection codecs would look the element up through kebs' entry point again (outside
    // this expansion, which recursive types never leave); kebs takes these apart itself, a user's own codec wins
    private def structural[F: Type]: Option[Expr[Codec[F]]] = if (Type[F] =:= Type.of[String]) None
    else
      Type[F] match {
        case IsOption(isOption) =>
          import isOption.Underlying as Item
          if (Type[F] =:= Type.of[Option[Item]])
            Some(shared[F](key[F]("structural"))(optionCodec[Item](codec[Item]).asInstanceOf[Expr[Codec[F]]]))
          else None
        case IsCollection(isCollection) =>
          import isCollection.Underlying as Item
          import isCollection.value.CtorResult
          isCollection.value.build match {
            case CtorLikeOf.PlainValue(ctor, _) =>
              isCollection.value.asMap match {
                case None =>
                  Some(shared[F](key[F]("structural")) {
                    collectionCodec[Item, CtorResult, F](
                      codec[Item],
                      isCollection.value.asIterable(_),
                      isCollection.value.factory,
                      ctor(_)
                    )
                  })
                case Some(isMap) =>
                  import isMap.{Key, Value}
                  if (Key =:= Type.of[String])
                    Some(shared[F](key[F]("structural")) {
                      stringMapCodec[Value, Item, CtorResult, F](
                        codec[Value],
                        isCollection.value.asIterable(_),
                        isCollection.value.factory,
                        pair => isMap.key(pair).asInstanceOf[Expr[String]],
                        isMap.value(_),
                        (k, v) => isMap.pair(k.asInstanceOf[Expr[Key]], v),
                        ctor(_)
                      )
                    })
                  else None
              }
            case _ => None
          }
        case _ => None
      }

    private def existing[F: Type]: Option[Expr[Codec[F]]] =
      summonInstance[Codec[F]](ignoredForFields)(using codecType[F]).map(found => shared[F](key[F]("existing"))(found))

    // flat although no codec of the field type exists yet: kebs 2 flattened Wrapper(inner: Inner) with Inner derived
    private def flat[F: Type]: Option[Expr[Codec[F]]] =
      CaseClass
        .parse[F]
        .toOption
        .flatMap(caseClass =>
          parameters(caseClass) match {
            case single :: Nil =>
              val fieldType: ?? = single.tpe
              import fieldType.Underlying as G
              Type.of[ValueClassLike[F, G]].summonExpr.toEither.toOption.map { rep =>
                shared[F](key[F]("flat"))(flatCodec[F, G](rep, codec[G]))
              }
            case _ => None
          }
        )

    final class ProductShape[T: Type](val caseClass: CaseClass[T]) {
      private val productType: Type[T] = Type[T]
      val fields: List[JsonField]      = parameters(caseClass).zipWithIndex.map { case (param, index) => new JsonField(param, index) }
      fields.find(field => field.transient && !field.param.hasDefault).foreach { field =>
        Environment.reportErrorAndAbort(s"@transientField ${field.name} of ${Type[T].prettyPrint} needs a default value")
      }

      // Option semantics (missing reads as None, None omitted) unless there is a codec of the Option type itself
      def optionItem(field: JsonField): Option[??] = {
        val fieldType: ?? = field.tpe
        import fieldType.Underlying as A
        if (existing[A].isDefined) None
        else
          Type[A] match {
            case IsOption(isOption) =>
              import isOption.Underlying as Item
              if (Type[A] =:= Type.of[Option[Item]]) Some(Type[Item].as_??) else None
            case _ => None
          }
      }

      def value[A: Type](obj: Expr[T], field: JsonField): Expr[A] =
        caseClass.caseFieldValuesAt(obj).get(field.name).map(_.value.asInstanceOf[Expr[A]]).getOrElse {
          // a field not accessible at the call site
          implicit val T: Type[T] = productType
          val index               = Expr(field.index)
          Expr.quote(Expr.splice(obj).asInstanceOf[Product].productElement(Expr.splice(index)).asInstanceOf[A])
        }

      // companion apply first (validating/normalising applys are honoured), then the constructor, then the Mirror
      def construct(arguments: List[Expr_??]): Expr[T] = {
        val byName = fields.map(_.name).zip(arguments).toMap
        viaCompanionApply[T](byName)
          .orElse(caseClass.construct[MIO](param => MIO.pure(byName(param.name))).unsafe.runSync._2 match {
            case Right(Some(constructed)) => Some(constructed)
            case _                        => None
          })
          .orElse(constructViaMirror[T](arguments))
          .getOrElse(Environment.reportErrorAndAbort(s"${Type[T].prettyPrint} has no accessible constructor or apply"))
      }
    }
  }

  private def parameters[F: Type](caseClass: CaseClass[F]): List[Parameter] =
    caseClass.primaryConstructor.parameters match {
      case single :: Nil => single.values.toList
      case Nil           => Nil
      case lists         =>
        Environment.reportErrorAndAbort(s"kebs derives case classes with one parameter list; ${Type[F].prettyPrint} has ${lists.size}")
    }

  private def defaultOf[A: Type](param: Parameter): Option[Expr[A]] = param.defaultValue.flatMap { method =>
    type EitherString[X] = Either[String, X]
    method
      .foldF[EitherString](
        onInstance = _ => Left("needs an instance"),
        onTypes = _ => Right(Map.empty),
        onValues = _ => Right(Map.empty)
      )
      .flatten
      .toOption
      .map(_.value.asInstanceOf[Expr[A]])
  }

  private def viaCompanionApply[F: Type](arguments: Map[String, Expr_??]): Option[Expr[F]] = {
    def walk(method: Method): Option[Expr[F]] = method match {
      case _: Method.OnInstance     => None
      case types: Method.ApplyTypes =>
        val typeParams = types.typeParams.flatten
        val typeArgs   = Type.typeArguments[F]
        if (typeParams.size == typeArgs.size) walk(types.apply(typeParams.zip(typeArgs).toMap)) else None
      case values: Method.ApplyValues =>
        val params = values.parameters.flatMap(_.toList)
        val fits   = params.size == arguments.size && params.forall { case (name, param) =>
          arguments.get(name).exists(argument => argument.Underlying <:< param.tpe.Underlying)
        }
        if (fits) walk(values.apply(params.map { case (name, _) => name -> arguments(name) }.toMap)) else None
      case result: Method.Result[?] =>
        import result.Returned
        if (Returned <:< Type[F]) result.build().toOption.map(_.asInstanceOf[Expr[F]]) else None
    }
    Type[F].unsortedMethodsNamed("apply").iterator.filter(_.isAvailable(AtCallSite)).map(walk).collectFirst { case Some(e) => e }
  }
}
