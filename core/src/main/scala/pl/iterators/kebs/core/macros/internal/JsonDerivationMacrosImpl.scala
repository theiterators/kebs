package pl.iterators.kebs.core.macros.internal

import hearth.MacroCommons
import hearth.fp.effect.MIO
import hearth.fp.instances.*
import hearth.std.StdExtensions
import pl.iterators.kebs.core.annotations.{fieldName, transientField}
import pl.iterators.kebs.core.macros.ValueClassLike

import scala.annotation.nowarn
import scala.collection.mutable
import scala.reflect.NameTransformer

private[kebs] sealed trait EntryPointRole
private[kebs] object EntryPointRole {
  case object StepAside   extends EntryPointRole
  case object Record      extends EntryPointRole
  case object UseExisting extends EntryPointRole
  case object Derive      extends EntryPointRole
}

// Derivation shared by the JSON modules; a backend generates the codec code. A root gives every type it needs, and that has
// no instance, a local implicit instance, and each of those derives one level: its fields are found by ordinary implicit
// search. So users' instances see derived and recursive types like any other, and nothing is derived inside a search.
private[kebs] trait JsonDerivationMacrosImpl { this: MacroCommons & StdExtensions =>

  // Scala 3: a private constructor is still reachable through the Mirror's fromProduct
  protected def constructViaMirror[F: Type](arguments: List[Expr_??]): Option[Expr[F]] = None

  protected def withFreshSearch[A](search: => A): A = search

  // code that uses the companion of F, or the object F
  protected def withPath[F: Type, A](code: Expr[A]): Expr[A] = code

  protected def expansionOwner: AnyRef
  protected def isExpandedInside(owner: AnyRef): Boolean

  protected def implicitEntry[A](body: => A): A = JsonDerivationState.entering(isExpandedInside)(body)
  protected def explicitEntry[A](body: => A): A = JsonDerivationState.asRoot(body)

  // the keys of the JsonPlaceholders in an expression
  protected def placeholderKeys[A](expr: Expr[A]): Set[String]

  // whether an expression uses the implicit that the root's instance hides, and whether that one is of a type
  protected def mentionsHidden[A](expr: Expr[A]): Boolean = false
  protected def isHidden[A: Type]: Boolean                = false

  // an instance of a type class of two types, found by the first one: a ValueClassLike or an InstanceConverter, and the type it converts to
  final protected class Wrapping(val instance: UntypedExpr, val base: ??)

  protected def valueClassOf[A: Type]: Option[Wrapping]
  protected def converterOf[A: Type]: Option[Wrapping]

  // the types of the instances that the methods made in an expression
  protected def madeBy[A](methods: Ignored, expr: Expr[A]): List[??] = Nil

  // implicit definitions a search passes over, as the compiler's symbols: hearth would describe every method of their owners
  final protected class Ignored(val symbols: List[Any]) {
    def ++(other: Ignored): Ignored = new Ignored(symbols ++ other.symbols)
    def isEmpty: Boolean            = symbols.isEmpty
  }
  protected val nothingIgnored: Ignored = new Ignored(Nil)

  // of A or its companion
  protected def methodsNamed[A: Type](names: Set[String]): Ignored
  protected def implicitsOf[A: Type]: Ignored
  protected def searchIgnoring[A: Type](ignored: Ignored): SummoningResult[A]

  // the compiler's own type, to recognise a type again
  protected def compilerType[A: Type]: AnyRef
  // the item of Option[Item], not of its subtypes
  protected def optionItemOf[A: Type]: Option[??]

  // tells types apart; hearth prints with syntax highlighting, and the same types are asked about again and again
  private val keys                     = new java.util.HashMap[AnyRef, String]
  protected def keyOf[A: Type]: String = keys.computeIfAbsent(compilerType[A], _ => Type[A].plainPrint)
  private def keyOf(tpe: ??): String   = {
    import tpe.Underlying as A
    keyOf[A]
  }

  private var extensionsLoaded = false

  private def loadExtensions(): Unit = if (!extensionsLoaded) {
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

  private val names                       = new java.util.HashMap[AnyRef, String]
  protected def fullName[T: Type]: String = names.computeIfAbsent(compilerType[T], _ => Type.fqcn[T])

  def isStandardLibrary[T: Type]: Boolean = {
    val name = fullName[T]
    name.startsWith("scala.") || name.startsWith("java.") || name.startsWith("(")
  }

  // pekko-http marshals its own sealed types (Done, entities); with a codec of one, it would be sent as JSON instead
  protected def derivesPekkoHierarchies: Boolean = false

  private def isPekkoHierarchy[T: Type]: Boolean =
    Type.isSealed[T] && !Type.isCaseClass[T] && Set("org.apache.pekko.", "akka.").exists(fullName[T].startsWith)

  def isLeftToItsLibrary[T: Type]: Boolean = isStandardLibrary[T] || (isPekkoHierarchy[T] && !derivesPekkoHierarchies)

  // An expansion's searches share its scope, so a search made again finds the same, and so does one made while discovering,
  // unless an entry point recorded a type on the way: then the instance found has a placeholder. An instance is handed out
  // once (discovery only looks at its own), so that no code is in the result twice.
  private val searches = mutable.Map.empty[(String, List[Any]), SummoningResult[?]]

  private def search[A: Type](ignored: Ignored): SummoningResult[A] = {
    def searched  = JsonDerivationState.searching(expansionOwner)(withFreshSearch(searchIgnoring[A](ignored)))
    val key       = (keyOf[A], ignored.symbols)
    val recording = JsonDerivationState.isRecording
    val result    =
      if (recording) searched
      else
        searches.remove(key) match {
          case Some(earlier) => earlier.asInstanceOf[SummoningResult[A]]
          case None          => searched
        }
    result match {
      case SummoningResult.Found(instance) => if (recording && placeholderKeys(instance).isEmpty) searches.update(key, result)
      case SummoningResult.NotFound(_)     => searches.update(key, result)
      case _                               => if (!recording) searches.update(key, result)
    }
    result
  }

  private def found[A: Type](result: SummoningResult[A]): Option[Expr[A]] = result match {
    case SummoningResult.Found(instance) => Some(instance)
    case SummoningResult.NotFound(_)     => None
    case SummoningResult.Ambiguous(_)    => Environment.reportErrorAndAbort(s"Ambiguous instances of ${Type[A].prettyPrint}")
    case SummoningResult.Diverging(_)    => Environment.reportErrorAndAbort(s"Diverging search for ${Type[A].prettyPrint}")
  }

  protected def summonInstance[A: Type](ignored: Ignored): Option[Expr[A]] = found(search[A](ignored))

  // an implicit entry point that is tried inside a level's search has nothing to offer: what it could derive is local already
  protected def entryPointRole[T: Type](typeClass: String, stepsAsideForExisting: Boolean)(hasInstance: => Boolean): EntryPointRole =
    entryPoint {
      def exists = JsonDerivationState.exists(s"$typeClass ${keyOf[T]}")(hasInstance)
      if (JsonDerivationState.isDiscovering) {
        if (JsonDerivationState.isInsisting || !exists) EntryPointRole.Record
        else {
          JsonDerivationState.yieldToExisting()
          EntryPointRole.StepAside
        }
      } else if (JsonDerivationState.isSearching) EntryPointRole.StepAside
      else if (!exists) EntryPointRole.Derive
      else if (stepsAsideForExisting) EntryPointRole.StepAside
      else EntryPointRole.UseExisting
    }

  private def entryPoint[A](body: => A): A =
    JsonDerivationState.entryPoint(error => macroAborts(error.getClass.getName))(body)

  private val macroAborts = Set("scala.reflect.macros.runtime.AbortMacroException", "scala.quoted.runtime.StopMacroExpansion")

  private lazy val FieldNameType: Type[fieldName]           = Type.of[fieldName]
  private lazy val TransientFieldType: Type[transientField] = Type.of[transientField]

  final class JsonField(val param: Parameter, val index: Int, owner: ??) {
    val name: String            = param.name
    val tpe: ??                 = param.tpe
    val transient: Boolean      = param.hasAnnotationOfType[transientField](using TransientFieldType)
    val renamed: Option[String] = param.annotationsOfType[fieldName](using FieldNameType).headOption.map { annotation =>
      Annotations.decodedConstructorArguments(annotation).getOrElse(Nil) match {
        case List(Right(name: String)) => name
        case _                         => Environment.reportErrorAndAbort(s"@fieldName on ${param.name} needs a literal String argument")
      }
    }
    def default[A: Type]: Option[Expr[A]] = {
      import owner.Underlying as T
      defaultOf[A](param).map(withPath[T, A](_))
    }
  }

  final class LocalInstance(val typeClass: String, val tpe: ??, val isCollection: Boolean)

  // rootIsImplicit: whether nested uses of the root's type go through its own local instance, the first one
  // refersToRoot: whether the root's type is among the types it needs, so that its instance must be in scope
  final class Needed(val instances: List[LocalInstance], val rootIsImplicit: Boolean, val refersToRoot: Boolean)

  final class LookUp(val exists: Boolean, derivable: => Boolean, val requests: List[(String, ??)], val usesHidden: Boolean) {
    def isDerivable: Boolean = derivable
  }

  trait Discoverable {
    def typeClass: String
    def lookUp[A: Type]: LookUp
    def parts[A: Type]: List[??]
    def isCollection[A: Type]: Boolean
    def isInline[A: Type]: Boolean
    def hides[A: Type]: Boolean
  }

  abstract class JsonDerivation[Codec[_]](val typeClass: String) extends Discoverable {
    // of the library's other type classes: a user's instance may need them too (a Format needs a Reads and a Writes)
    protected def siblings: List[Discoverable] = Nil
    protected def codecType[A: Type]: Type[Codec[A]]
    protected def entryPoints: Ignored
    // the library's own instances of generic types, where they would be ambiguous with a user's
    protected def libraryContainers: Ignored
    protected def libraryOptions: Ignored                 = libraryContainers
    protected def derivesFromStandardLibrary: Set[String] = Set.empty
    // the elements of a collection that the library's own instances are not used for, because next to local instances they
    // would also accept one of the elements' supertype
    protected def elementOf[F: Type]: Option[??]                                    = None
    protected def collectionCodec[F: Type, X: Type](codecs: Codecs): Expr[Codec[F]] = rejectMissing[F]
    protected def libraryCollections: Ignored                                       = nothingIgnored

    // a codec made of the codecs of other types: of a type that is written as another one, or of a standard type that the library
    // has no instance of
    final protected class Composite[F](val parts: List[??], val build: Codecs => Expr[Codec[F]])
    protected def composite[F: Type]: Option[Composite[F]] = None
    // whether an Option is written as something else
    protected def isConverted[F: Type]: Boolean = false
    // at their own types, not only as cases of a sealed hierarchy
    protected def derivesObjects: Boolean = true

    protected def singletonCodec[T: Type](value: Expr[T]): Expr[Codec[T]]
    // functions, so that everything else is made outside the code that uses the codecs, which is made of nested quotes
    protected def productCodec[T: Type](product: ProductShape[T]): Codecs => Expr[Codec[T]]
    // leaves of a sealed hierarchy (nested hierarchies flattened), by case name
    protected def sumCodec[T: Type](leaves: List[(String, ??)]): Codecs => Expr[Codec[T]]
    // whose codecs productCodec uses
    protected def partsOf[T](product: ProductShape[T]): List[??] =
      product.fields.filterNot(_.transient).map(field => product.optionItem(field).getOrElse(field.tpe))

    private val derivable = mutable.Map.empty[String, Boolean]

    // a quote on Scala 3, unpickled at each use
    private val codecTypes                               = new java.util.HashMap[AnyRef, AnyRef]
    final protected def codecOf[A: Type]: Type[Codec[A]] =
      codecTypes.computeIfAbsent(compilerType[A], _ => codecType[A].asInstanceOf[AnyRef]).asInstanceOf[Type[Codec[A]]]

    final class Codecs(indices: Map[String, Int], made: Set[String], val local: Expr[LocalCodecs]) {
      // whether there is no instance of the type
      def isMade[F: Type]: Boolean       = made(keyOf[F])
      def indexOf[F: Type]: Int          = indices(keyOf[F])
      def apply[F: Type]: Expr[Codec[F]] = codecAt[Codec[F]](local, indexOf[F])(using codecOf[F])
    }

    final def oneLevel[T: Type]: Expr[Codec[T]] = JsonDerivationState.asRoot {
      loadExtensions()
      derivation[T].orElse(composite[T].map(made(_))).getOrElse(rejectMissing[T]).apply()
    }

    private def made[F: Type](composite: Composite[F]): () => Expr[Codec[F]] = () => withCodecs[F](composite.parts)(composite.build)

    // made at each use, and local only where a user's instance needs one
    final def isInline[F: Type]: Boolean = isStandardLibrary[F] && composite[F].isDefined

    private def withCodecs[T: Type](parts: List[??], areCases: Boolean = false)(build: Codecs => Expr[Codec[T]]): Expr[Codec[T]] = {
      val distinct = parts.distinctBy(keyOf(_))
      val missing  = mutable.Set.empty[String]
      val creators = distinct.map { part =>
        import part.Underlying as F
        implicit val CodecF: Type[Codec[F]] = codecOf[F]
        val instance                        = found(instanceAtLevelOf[F])
        if (instance.isEmpty) missing += keyOf[F]
        val ofObject = if (areCases) objectCodec[F] else None
        creator[Codec[F]](instance.orElse(ofObject.map(_.apply())).orElse(inlineCodec[F]).getOrElse(rejectMissing[F]))
      }
      val indices = distinct.map(keyOf(_)).zipWithIndex.toMap
      withLocalCodecs[Codec[T]](creators)(local => build(new Codecs(indices, missing.toSet, local)))(using codecOf[T])
    }

    private def inlineCodec[F: Type]: Option[Expr[Codec[F]]] =
      if (isInline[F]) composite[F].map(composite => unlessInProgress[F, Expr[Codec[F]]]("")(made(composite).apply())) else None

    private var inProgress = Set.empty[String]

    // a type that is converted to itself would be followed endlessly
    final protected def unlessInProgress[F: Type, A](purpose: String)(body: => A): A = {
      val name = purpose + keyOf[F]
      if (inProgress(name)) Environment.reportErrorAndAbort(s"${Type[F].prettyPrint} is converted to itself")
      inProgress += name
      try body
      finally inProgress -= name
    }

    final def isCollection[A: Type]: Boolean = elementOf[A].isDefined

    final def hides[A: Type]: Boolean = isHidden[Codec[A]](using codecOf[A])

    protected def rejectMissing[F: Type]: Nothing = Environment.reportErrorAndAbort(
      s"No ${codecOf[F].prettyPrint} in scope, and kebs derives one only for case classes and sealed hierarchies outside the standard library and pekko"
    )

    final def recorded[A: Type]: Expr[Codec[A]] = entryPoint {
      if (!isDerivable[A] && !isInline[A]) Environment.reportErrorAndAbort(s"${Type[A].prettyPrint} is not derived here")
      val key = s"$typeClass ${keyOf[A]}"
      JsonDerivationState.request(JsonDerivationState.Request(typeClass, key, UntypedType.fromTyped[A].asInstanceOf[AnyRef]))
      placeholder[Codec[A]](key)(using codecOf[A])
    }

    // the instances a root needs as local ones, its own first; hidesExisting: it has the name of the implicit being defined
    final def needed[T: Type](preferExisting: Boolean, hidesExisting: Boolean = false): Needed = JsonDerivationState.inOneScope {
      if (!isDerivable[T] && !isInline[T]) rejectMissing[T]
      val hidden    = if (hidesExisting) Some(Type[T].as_??) else None
      val discovery = new Discovery(Type[T].prettyPrint, this :: siblings, hidden)
      discovery.derive[T](this)
      implicit val CodecT: Type[Codec[T]] = codecOf[T]
      val (result, requests)              =
        if (preferExisting) JsonDerivationState.discovering(instanceOf[T])
        else (SummoningResult.NotFound(CodecT): SummoningResult[Codec[T]], new JsonDerivationState.Requests)
      val exists = found(result).isDefined
      if (exists) requestedBy(result, requests).foreach(discovery.visit)
      new Needed(discovery.instances, rootIsImplicit = !exists, discovery.refersToRoot)
    }

    // the instances that a root needs as local ones when it has an instance itself
    final def neededBeside[T: Type](ignored: Ignored): Needed = JsonDerivationState.inOneScope {
      implicit val CodecT: Type[Codec[T]] = codecOf[T]
      val discovery                       = new Discovery(Type[T].prettyPrint, this :: siblings, hidden = None)
      val (result, requests)              = JsonDerivationState.discovering(search[Codec[T]](ignored))
      requestedBy(result, requests).foreach(discovery.visit)
      new Needed(discovery.instances, rootIsImplicit = true, refersToRoot = true)
    }

    // not those of candidates that were tried on the way
    private def requestedBy[A](result: SummoningResult[A], requests: JsonDerivationState.Requests): List[(String, ??)] = {
      def typed(request: JsonDerivationState.Request): (String, ??) = request.typeClass -> request.tpe.asInstanceOf[UntypedType].as_??
      result match {
        case SummoningResult.Found(instance) =>
          requests.toList.filter(request => placeholderKeys(instance).contains(request.key)).map(typed) ++
          madeBy(libraryCollections, instance).map(typeClass -> _)
        case _ => requests.toList.map(typed)
      }
    }

    final def lookUp[A: Type]: LookUp = {
      implicit val CodecA: Type[Codec[A]] = codecOf[A]
      val (result, requests)              = JsonDerivationState.discovering(instanceOf[A])
      val requested                       = requestedBy(result, requests)
      lazy val derivable                  = isDerivable[A]
      val exists                          = result match {
        case SummoningResult.Found(_) => true
        case _                        => derivable && found(result).isDefined
      }
      val needsItself = requested.exists { case (of, tpe) => of == typeClass && tpe.Underlying =:= Type[A] }
      if ((!exists || needsItself) && derivable && !isCollection[A] && composite[A].isEmpty) {
        rejectFlat[A]()
        if (!exists && requests.yielded) rejectUnusable[A]()
      }
      val usesHidden = result match {
        case SummoningResult.Found(instance) => mentionsHidden(instance)
        case _                               => false
      }
      new LookUp(exists, derivable, requested, usesHidden)
    }

    // found neither way, or only through itself: deriving it as an object would change its shape
    private def rejectFlat[A: Type](): Unit =
      for (caseClass <- CaseClass.parse[A].toOption) new ProductShape[A](caseClass).fields match {
        case List(field) =>
          val fieldType: ?? = field.tpe
          import fieldType.Underlying as F
          implicit val Rep: Type[ValueClassLike[A, F]] = Type.of[ValueClassLike[A, F]]
          if (summonInstance[ValueClassLike[A, F]](nothingIgnored).isDefined)
            Environment.reportErrorAndAbort(
              s"${Type[A].prettyPrint} is written as its only field, and there is no ${codecOf[F].prettyPrint} without one of ${Type[A].prettyPrint}"
            )
        case _ => ()
      }

    private def rejectUnusable[A: Type](): Unit = {
      implicit val CodecA: Type[Codec[A]] = codecOf[A]
      JsonDerivationState.discovering(JsonDerivationState.insistingOn(search[Codec[A]](entryPoints)))._1 match {
        case SummoningResult.Found(_) =>
          Environment.reportErrorAndAbort(
            s"There is a ${CodecA.prettyPrint}, but what it needs is in scope only as a less specific type class (a RootJsonFormat that is a JsonFormat, an OWrites that is a Writes)"
          )
        case _ => ()
      }
    }

    final def parts[A: Type]: List[??] =
      if (composite[A].isDefined) composite[A].toList.flatMap(_.parts)
      else if (Expr.singletonOf[A].isDefined) Nil
      else
        CaseClass.parse[A].toOption match {
          case Some(caseClass) => new ProductShape[A](caseClass).fields.filterNot(_.transient).map(_.tpe)
          case None            => elementOf[A].map(List(_)).orElse(leavesOf[A].map(_.map(_._2))).getOrElse(Nil)
        }

    // a user's instance first: next to the library's own for the same container it would be ambiguous
    private def instanceOf[A: Type]: SummoningResult[Codec[A]] = {
      implicit val CodecA: Type[Codec[A]] = codecOf[A]
      if (isCollection[A]) search[Codec[A]](entryPoints ++ libraryContainers)
      else if (Type.typeArguments[A].isEmpty || libraryContainers.isEmpty) search[Codec[A]](entryPoints)
      else
        search[Codec[A]](entryPoints ++ libraryContainers) match {
          case SummoningResult.NotFound(_) => search[Codec[A]](entryPoints)
          case result                      => result
        }
    }

    // a collection without a local instance is the library's after all
    private def instanceAtLevelOf[A: Type]: SummoningResult[Codec[A]] = {
      implicit val CodecA: Type[Codec[A]] = codecOf[A]
      instanceOf[A] match {
        case SummoningResult.NotFound(_) if isCollection[A] => search[Codec[A]](entryPoints)
        case result                                         => result
      }
    }

    final def isDerivable[A: Type]: Boolean = derivable.getOrElseUpdate(keyOf[A], derivation[A].isDefined)

    // standard library sums and tuples have the library's own codecs; deriving them would silently change their shape
    private def derivation[F: Type]: Option[() => Expr[Codec[F]]] =
      elementOf[F] match {
        case Some(element) =>
          import element.Underlying as X
          Some(() => withCodecs[F](List(element))(codecs => collectionCodec[F, X](codecs)))
        case None => (if (isInline[F]) None else composite[F].map(made(_))).orElse(ownDerivation[F])
      }

    private def ownDerivation[F: Type]: Option[() => Expr[Codec[F]]] =
      if (isLeftToItsLibrary[F] && !derivesFromStandardLibrary(fullName[F])) None
      else if (!derivesObjects && Expr.singletonOf[F].isDefined) None
      else
        objectCodec[F]
          .orElse(CaseClass.parse[F].toOption.map { caseClass => () =>
            val product = new ProductShape[F](caseClass)
            withCodecs[F](partsOf(product))(productCodec[F](product))
          })
          .orElse(leavesOf[F].map(leaves => () => withCodecs[F](leaves.map(_._2), areCases = true)(sumCodec[F](leaves))))

    private def objectCodec[F: Type]: Option[() => Expr[Codec[F]]] =
      Expr.singletonOf[F].map(value => () => singletonCodec[F](withPath[F, F](value)))

    // nested sealed hierarchies are flattened to their leaves
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
            else List(NameTransformer.decode(name) -> Type[C].as_??)
          })
          .filter(_.nonEmpty)
          .map(_.distinctBy(leaf => keyOf(leaf._2)))

    final class ProductShape[T: Type](val caseClass: CaseClass[T]) {
      private val productType: Type[T] = Type[T]
      val fields: List[JsonField]      = parameters(caseClass).zipWithIndex.map { case (param, index) =>
        new JsonField(param, index, Type[T].as_??)
      }
      fields.find(field => field.transient && !field.param.hasDefault).foreach { field =>
        Environment.reportErrorAndAbort(s"@transientField ${field.name} of ${Type[T].prettyPrint} needs a default value")
      }

      // Option semantics (missing reads as None, None omitted) unless there is a codec of the Option type itself
      def optionItem(field: JsonField): Option[??] = optionItems.get(field.index)

      private lazy val optionItems: Map[Int, ??] = fields.flatMap { field =>
        val fieldType: ?? = field.tpe
        import fieldType.Underlying as A
        optionItemOf[A]
          .filter { _ =>
            implicit val CodecA: Type[Codec[A]] = codecOf[A]
            summonInstance[Codec[A]](entryPoints ++ libraryOptions).isEmpty && !isConverted[A]
          }
          .map(field.index -> _)
      }.toMap

      // hearth makes the values of all fields at once
      private var valuesAt: Option[(Expr[T], Map[String, Expr_??])] = None
      private def fieldValues(obj: Expr[T]): Map[String, Expr_??]   = valuesAt match {
        case Some((at, values)) if at.asInstanceOf[AnyRef] eq obj.asInstanceOf[AnyRef] => values
        case _                                                                         =>
          val values = caseClass.caseFieldValuesAt(obj).toMap
          valuesAt = Some((obj, values))
          values
      }

      def value[A: Type](obj: Expr[T], field: JsonField): Expr[A] =
        fieldValues(obj).get(field.name).map(_.value.asInstanceOf[Expr[A]]).getOrElse {
          // a field not accessible at the call site
          implicit val T: Type[T] = productType
          val index               = Expr(field.index)
          Expr.quote(Expr.splice(obj).asInstanceOf[Product].productElement(Expr.splice(index)).asInstanceOf[A])
        }

      // companion apply first (validating/normalising applys are honoured), then the constructor, an overload of apply that
      // only accepts the arguments, and the Mirror
      def construct(arguments: List[Expr_??]): Expr[T] = {
        val byName      = fields.map(_.name).zip(arguments).toMap
        val constructed = viaCompanionApply[T](byName, exactly = true)
          .orElse(caseClass.construct[MIO](param => MIO.pure(byName(param.name))).unsafe.runSync._2 match {
            case Right(Some(constructed)) => Some(constructed)
            case _                        => None
          })
          .orElse(viaCompanionApply[T](byName, exactly = false))
          .orElse(constructViaMirror[T](arguments))
          .getOrElse(Environment.reportErrorAndAbort(s"${Type[T].prettyPrint} has no accessible constructor or apply"))
        withPath[T, T](constructed)
      }
    }
  }

  // made for every field or case; built directly, as hearth's cross-quotes unpickle a quote at each use on Scala 3
  protected def codecAt[C: Type](local: Expr[LocalCodecs], index: Int): Expr[C]
  protected def listOf[A: Type](parts: List[Expr[A]]): Expr[List[A]]
  protected def statements(parts: List[Expr[Unit]]): Expr[Unit]

  private def creator[C: Type](instance: Expr[C]): Expr[() => Any] = Expr.quote((() => Expr.splice(instance)): (() => Any))

  private def withLocalCodecs[C: Type](creators: List[Expr[() => Any]])(build: Expr[LocalCodecs] => Expr[C]): Expr[C] =
    if (creators.isEmpty) build(Expr.quote(null.asInstanceOf[LocalCodecs]))
    else {
      implicit val Creator: Type[() => Any] = Type.of[() => Any]
      val list                              = listOf(creators)
      Expr.quote {
        val codecs = LocalCodecs(Expr.splice(list))
        Expr.splice(build(Expr.quote(codecs)))
      }
    }

  // whether a value is of a case, by the order of the cases, which is also that of their codecs
  protected def caseTests(leaves: List[(String, ??)]): Expr[List[Any => Boolean]] = {
    implicit val Test: Type[Any => Boolean] = Type.of[Any => Boolean]
    listOf(leaves.map { case (_, leaf) =>
      import leaf.Underlying as C
      caseTest[C]
    })
  }

  // C is the concrete leaf type in the expansion; only this macro's own source sees it abstract
  @nowarn("msg=is unchecked since it is eliminated by erasure")
  private def caseTest[C: Type]: Expr[Any => Boolean] = Expr.quote((value: Any) => value.isInstanceOf[C])

  private def placeholder[A: Type](key: String): Expr[A] = {
    val keyExpr = Expr(key)
    Expr.quote(JsonPlaceholder[A](Expr.splice(keyExpr)))
  }

  // Types are walked, not only searched for: before the local instances exist, the search for a recursive container may fail.
  final private class Discovery(root: => String, derivations: List[Discoverable], hidden: Option[??]) {
    private val seen                   = mutable.Set.empty[(String, String, Boolean)]
    private val locals                 = mutable.ListBuffer.empty[LocalInstance]
    def instances: List[LocalInstance] = locals.toList
    private var rootType               = Option.empty[String]
    var refersToRoot                   = false

    def derive[A: Type](derivation: Discoverable): Unit = {
      if (rootType.isEmpty) rootType = Some(keyOf[A])
      seen ++= List(true, false).map((derivation.typeClass, keyOf[A], _))
      locals += new LocalInstance(derivation.typeClass, Type[A].as_??, derivation.isCollection[A])
      derivation.parts[A].foreach(visit(derivation, _))
    }

    def visit(request: (String, ??)): Unit =
      derivations.find(_.typeClass == request._1).foreach(visit(_, request._2, requested = true))

    private def visit(derivation: Discoverable, tpe: ??, requested: Boolean = false): Unit = {
      import tpe.Underlying as A
      if (rootType.contains(keyOf[A])) refersToRoot = true
      val isWanted = requested && isStandardLibrary[A]
      if ((isWanted || mayNeedInstance[A]) && seen.add((derivation.typeClass, keyOf[A], isWanted))) {
        if (nesting[A] > MaxNesting)
          Environment.reportErrorAndAbort(s"$root refers to ever larger types (${Type[A].plainPrint.take(80)}...)")
        if (hidden.exists(_.Underlying =:= Type[A])) derive[A](derivation)
        else {
          val lookUp        = derivation.lookUp[A]
          lazy val isInline = derivation.isInline[A]
          if (!lookUp.exists && (lookUp.isDerivable || isWanted && isInline)) derive[A](derivation)
          else {
            lookUp.requests.foreach(visit)
            if (lookUp.usesHidden) for (tpe <- hidden) {
              import tpe.Underlying as T
              derivations.filter(_.hides[T]).foreach(visit(_, tpe))
            }
            if (!lookUp.exists) (if (isInline) derivation.parts[A] else Type.typeArguments[A]).foreach(visit(derivation, _))
          }
        }
      }
    }
  }

  private def mayNeedInstance[A: Type]: Boolean = !isStandardLibrary[A] || Type.typeArguments[A].nonEmpty

  private def nesting[A: Type]: Int = Type.typeArguments[A].foldLeft(0) { (deepest, tpe) =>
    import tpe.Underlying as X
    deepest.max(1 + nesting[X])
  }

  // a type that contains an ever larger application of itself would be walked endlessly
  private val MaxNesting = 12

  private def parameters[F: Type](caseClass: CaseClass[F]): List[Parameter] =
    caseClass.primaryConstructor.parameters match {
      case single :: Nil => single.values.toList
      case Nil           => Nil
      case lists         =>
        Environment.reportErrorAndAbort(s"kebs derives case classes with one parameter list; ${Type[F].prettyPrint} has ${lists.size}")
    }

  private def defaultOf[A: Type](param: Parameter): Option[Expr[A]] =
    param.defaultValue.filter(_.isAvailable(AtCallSite)).flatMap { method =>
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

  private def viaCompanionApply[F: Type](arguments: Map[String, Expr_??], exactly: Boolean): Option[Expr[F]] = {
    def walk(method: Method): Option[Expr[F]] = method match {
      case _: Method.OnInstance     => None
      case types: Method.ApplyTypes =>
        val typeParams = types.typeParams.flatten
        val typeArgs   = Type.typeArguments[F]
        if (typeParams.size == typeArgs.size) walk(types.apply(typeParams.zip(typeArgs).toMap)) else None
      case values: Method.ApplyValues =>
        val params = values.parameters.flatMap(_.toList)
        val fits   = params.size == arguments.size && params.forall { case (name, param) =>
          arguments.get(name).exists { argument =>
            if (exactly) argument.Underlying =:= param.tpe.Underlying else argument.Underlying <:< param.tpe.Underlying
          }
        }
        if (fits) walk(values.apply(params.map { case (name, _) => name -> arguments(name) }.toMap)) else None
      case result: Method.Result[?] =>
        import result.Returned
        if (Returned <:< Type[F]) result.build().toOption.map(_.asInstanceOf[Expr[F]]) else None
    }
    Type[F].unsortedMethodsNamed("apply").iterator.filter(_.isAvailable(AtCallSite)).map(walk).collectFirst { case Some(e) => e }
  }
}
