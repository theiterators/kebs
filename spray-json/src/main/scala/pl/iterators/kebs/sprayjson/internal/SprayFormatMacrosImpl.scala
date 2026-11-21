package pl.iterators.kebs.sprayjson.internal

import hearth.MacroCommons
import hearth.fp.effect.MIO
import hearth.fp.instances.*
import hearth.std.StdExtensions
import pl.iterators.kebs.core.annotations.{fieldName, transientField}
import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.macros.ValueClassLike
import pl.iterators.kebs.sprayjson.KebsSprayJson
import spray.json.{CollectionFormats, JsValue, JsonFormat, RootJsonFormat, StandardFormats}

import scala.annotation.nowarn
import scala.collection.mutable

private[sprayjson] trait SprayFormatMacrosImpl { this: MacroCommons & StdExtensions =>

  // Scala 3: a private constructor is still reachable through the Mirror's fromProduct
  protected def constructViaMirror[F: Type](arguments: List[Expr_??]): Option[Expr[F]] = None

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

  private def protocolMethods(names: Set[String]): Seq[UntypedMethod] =
    Type.of[KebsSprayJson].unsortedMethods.filter(method => names(method.name)).map(_.asUntyped)

  private lazy val derivationEntryPoints = protocolMethods(Set("kebsAutoJsonFormat", "kebsAutoJsonFormatSealed"))
  // kebs' own generic formats do not count as an existing root instance: a root request derives an object (kebs 2)
  private lazy val ignoredAtRoot =
    derivationEntryPoints ++ protocolMethods(Set("jsonFlatFormat", "jsonConversionFormat2"))
  // spray's Option/collection formats would look the element up through kebs' entry point again (outside this
  // expansion, which recursive types never leave); kebs takes these apart itself, a user's own container format wins
  private lazy val ignoredForFields = derivationEntryPoints ++
    (Type.of[CollectionFormats].unsortedMethods ++ Type.of[StandardFormats].unsortedMethods.filter(_.name == "optionFormat"))
      .filter(_.isImplicit)
      .map(_.asUntyped)

  private def summonFormat[F: Type](ignored: Seq[UntypedMethod]): Option[Expr[JsonFormat[F]]] =
    Type.of[JsonFormat[F]].summonExprIgnoring(ignored*) match {
      case SummoningResult.Found(found) => Some(found)
      case SummoningResult.Ambiguous(_) =>
        Environment.reportErrorAndAbort(s"Ambiguous JsonFormat instances for ${Type[F].prettyPrint}")
      case _ => None
    }

  def isBeingDerived[T: Type]: Boolean = SprayDerivationGuard.isBeingDerived(Type[T].plainPrint)

  def isStandardLibrary[T: Type]: Boolean = {
    val name = Type.fqcn[T]
    name.startsWith("scala.") || name.startsWith("java.") || name.startsWith("(")
  }

  // implicit entry points consult existing formats first; an explicit jsonFormatN/jsonFormatSealed call always derives.
  // stepAside: fail when a format exists, for an entry point whose failure only skips it
  def deriveRootFormat[T: Type](config: Expr[JsonConfig], consultExisting: Boolean, stepAside: Boolean): Expr[RootJsonFormat[T]] = {
    loadExtensions()
    (if (consultExisting) summonFormat[T](ignoredAtRoot) else None) match {
      case Some(found) =>
        if (stepAside) Environment.reportErrorAndAbort(s"${Type[T].prettyPrint} already has a JsonFormat")
        Expr.quote(SprayDerivationRuntime.asRoot(Expr.splice(found)))
      case None => new Derivation(config).root[T]
    }
  }

  private lazy val FieldNameType: Type[fieldName]           = Type.of[fieldName]
  private lazy val TransientFieldType: Type[transientField] = Type.of[transientField]

  final private class Field(val param: Parameter, val index: Int) {
    val transient: Boolean      = param.hasAnnotationOfType[transientField](using TransientFieldType)
    val renamed: Option[String] = param.annotationsOfType[fieldName](using FieldNameType).headOption.map { annotation =>
      Annotations.decodedConstructorArguments(annotation).getOrElse(Nil) match {
        case List(Right(name: String)) => name
        case _                         => Environment.reportErrorAndAbort(s"@fieldName on ${param.name} needs a literal String argument")
      }
    }
  }

  final private class Derivation(config: Expr[JsonConfig]) {
    private var cache: ValDefsCache = ValDefsCache.empty
    private var declared            = Set.empty[String]

    private def key[F: Type](kind: String): String = kind + ":" + Type[F].plainPrint

    def root[T: Type]: Expr[RootJsonFormat[T]] = {
      implicit val RootT: Type[RootJsonFormat[T]] = Type.of[RootJsonFormat[T]]
      val format                                  = derived[T].getOrElse(
        Environment.reportErrorAndAbort(
          s"Cannot derive a RootJsonFormat for ${Type[T].prettyPrint}: not a case class or a sealed hierarchy"
        )
      )
      cache.toValDefs.use(_ => format)
    }

    // forward-declared before its body is built, so that recursive references resolve to it
    private def derived[F: Type]: Option[Expr[RootJsonFormat[F]]] = {
      implicit val RootF: Type[RootJsonFormat[F]] = Type.of[RootJsonFormat[F]]
      val k                                       = key[F]("derived")
      if (declared(k)) cache.get0Ary[RootJsonFormat[F]](k)
      else
        derivation[F].map { build =>
          val builder = ValDefBuilder.ofLazy[RootJsonFormat[F]]("kebsFormat")
          cache = builder.forwardDeclare(cache, k)
          declared += k
          val body = SprayDerivationGuard.deriving(Type[F].plainPrint)(build())
          cache = builder.buildCachedWith(cache, k)(_ => body)
          cache.get0Ary[RootJsonFormat[F]](k).get
        }
    }

    // standard library sums and tuples have spray formats; deriving them would silently change their JSON shape
    private def derivation[F: Type]: Option[() => Expr[RootJsonFormat[F]]] =
      if (isStandardLibrary[F] && (Type.isSealed[F] || Type.fqcn[F].startsWith("scala.Tuple") || Type.fqcn[F].startsWith("(")))
        None
      else
        Expr
          .singletonOf[F]
          .map(value => () => Expr.quote(SprayDerivationRuntime.singleton[F](Expr.splice(value))))
          .orElse(CaseClass.parse[F].toOption.map(caseClass => () => product[F](caseClass)))
          .orElse(leavesOf[F].map(leaves => () => sum[F](leaves)))

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

    private def sum[F: Type](leaves: List[(String, ??)]): Expr[RootJsonFormat[F]] = {
      implicit val StringType: Type[String]           = Type.of[String]
      implicit val ListStringType: Type[List[String]] = Type.of[List[String]]
      val caseNames                                   = Expr(leaves.map(_._1))
      Expr.quote {
        SprayDerivationRuntime.sum[F](Expr.splice(config), Expr.splice(caseNames))(
          (obj: F, wrap: (Int, JsValue) => JsValue) => Expr.splice(writeCase[F](leaves, Expr.quote(obj), Expr.quote(wrap))),
          (index: Int, json: JsValue) => Expr.splice(readCase[F](leaves, Expr.quote(index), Expr.quote(json)))
        )
      }
    }

    // C is the concrete leaf type in the expansion; only this macro's own source sees it abstract
    @nowarn("msg=is unchecked since it is eliminated by erasure")
    private def writeCase[F: Type](
        leaves: List[(String, ??)],
        obj: Expr[F],
        wrap: Expr[(Int, JsValue) => JsValue]
    ): Expr[JsValue] =
      leaves.zipWithIndex.foldRight(Expr.quote(SprayDerivationRuntime.unreachableCase[JsValue](Expr.splice(obj)))) {
        case (((_, leaf), index), otherwise) =>
          import leaf.Underlying as C
          val i = Expr(index)
          Expr.quote {
            if (Expr.splice(obj).isInstanceOf[C])
              Expr.splice(wrap).apply(Expr.splice(i), Expr.splice(format[C]).write(Expr.splice(obj).asInstanceOf[C]))
            else Expr.splice(otherwise)
          }
      }

    private def readCase[F: Type](leaves: List[(String, ??)], index: Expr[Int], json: Expr[JsValue]): Expr[F] =
      leaves.zipWithIndex.foldRight(Expr.quote(SprayDerivationRuntime.unreachableCase[F](Expr.splice(index)))) {
        case (((_, leaf), i), otherwise) =>
          import leaf.Underlying as C
          val ii = Expr(i)
          Expr.quote {
            if (Expr.splice(index) == Expr.splice(ii)) Expr.splice(format[C]).read(Expr.splice(json)).asInstanceOf[F]
            else Expr.splice(otherwise)
          }
      }

    private def shared[F: Type](k: String)(format: => Expr[JsonFormat[F]]): Expr[JsonFormat[F]] = {
      implicit val JsonFormatF: Type[JsonFormat[F]] = Type.of[JsonFormat[F]]
      if (!declared(k)) {
        val value = format
        cache = ValDefBuilder.ofLazy[JsonFormat[F]]("kebsFormat").buildCachedWith(cache, k)(_ => value)
        declared += k
      }
      cache.get0Ary[JsonFormat[F]](k).get
    }

    def format[F: Type]: Expr[JsonFormat[F]] = {
      implicit val JsonFormatF: Type[JsonFormat[F]] = Type.of[JsonFormat[F]]
      implicit val RootF: Type[RootJsonFormat[F]]   = Type.of[RootJsonFormat[F]]
      if (declared(key[F]("derived"))) cache.get0Ary[RootJsonFormat[F]](key[F]("derived")).get.upcast[JsonFormat[F]]
      else
        existing[F]
          .orElse(structural[F])
          .orElse(flat[F])
          .orElse(derived[F].map(_.upcast[JsonFormat[F]]))
          .getOrElse(
            Environment.reportErrorAndAbort(
              s"No JsonFormat for ${Type[F].prettyPrint} in scope, and kebs derives one only for case classes and " +
                "sealed hierarchies outside the standard library (a recursive type through a tuple or an Either needs " +
                "an explicit implicit lazy val)"
            )
          )
    }

    private def structural[F: Type]: Option[Expr[JsonFormat[F]]] = if (Type[F] =:= Type.of[String]) None
    else
      Type[F] match {
        case IsOption(isOption) =>
          import isOption.Underlying as Item
          if (Type[F] =:= Type.of[Option[Item]])
            Some(shared[F](key[F]("structural")) {
              Expr.quote(SprayDerivationRuntime.optionFormat[Item](Expr.splice(format[Item]))).asInstanceOf[Expr[JsonFormat[F]]]
            })
          else None
        case IsCollection(isCollection) =>
          import isCollection.Underlying as Item
          import isCollection.value.CtorResult
          isCollection.value.build match {
            case CtorLikeOf.PlainValue(ctor, _) =>
              isCollection.value.asMap match {
                case None =>
                  Some(shared[F](key[F]("structural")) {
                    Expr.quote {
                      SprayDerivationRuntime.collectionFormat[Item, CtorResult, F](
                        Expr.splice(format[Item]),
                        (collection: F) => Expr.splice(isCollection.value.asIterable(Expr.quote(collection))),
                        Expr.splice(isCollection.value.factory)
                      )((builder: mutable.Builder[Item, CtorResult]) => Expr.splice(ctor(Expr.quote(builder))))
                    }
                  })
                case Some(isMap) =>
                  import isMap.{Key, Value}
                  if (Key =:= Type.of[String])
                    Some(shared[F](key[F]("structural")) {
                      Expr.quote {
                        SprayDerivationRuntime.stringMapFormat[Value, Item, CtorResult, F](
                          Expr.splice(format[Value]),
                          (collection: F) => Expr.splice(isCollection.value.asIterable(Expr.quote(collection))),
                          Expr.splice(isCollection.value.factory)
                        )(
                          (pair: Item) => Expr.splice(isMap.key(Expr.quote(pair))).asInstanceOf[String],
                          (pair: Item) => Expr.splice(isMap.value(Expr.quote(pair))),
                          (k: String, v: Value) => Expr.splice(isMap.pair(Expr.quote(k).asInstanceOf[Expr[Key]], Expr.quote(v))),
                          (builder: mutable.Builder[Item, CtorResult]) => Expr.splice(ctor(Expr.quote(builder)))
                        )
                      }
                    })
                  else None
              }
            case _ => None
          }
        case _ => None
      }

    private def existing[F: Type]: Option[Expr[JsonFormat[F]]] =
      summonFormat[F](ignoredForFields).map(found => shared[F](key[F]("existing"))(found))

    // flat although no format of the field type exists yet: kebs 2 flattened Wrapper(inner: Inner) with Inner derived
    private def flat[F: Type]: Option[Expr[JsonFormat[F]]] =
      CaseClass
        .parse[F]
        .toOption
        .flatMap(caseClass =>
          parameters(caseClass) match {
            case single :: Nil =>
              val fieldType: ?? = single.tpe
              import fieldType.Underlying as G
              Type.of[ValueClassLike[F, G]].summonExpr.toEither.toOption.map { rep =>
                shared[F](key[F]("flat")) {
                  Expr.quote(SprayDerivationRuntime.flatFormat[F, G](Expr.splice(rep), Expr.splice(format[G])))
                }
              }
            case _ => None
          }
        )

    private def parameters[F: Type](caseClass: CaseClass[F]): List[Parameter] =
      caseClass.primaryConstructor.parameters match {
        case single :: Nil => single.values.toList
        case Nil           => Nil
        case lists         =>
          Environment.reportErrorAndAbort(
            s"kebs derives case classes with one parameter list; ${Type[F].prettyPrint} has ${lists.size}"
          )
      }

    private def product[F: Type](caseClass: CaseClass[F]): Expr[RootJsonFormat[F]] = {
      implicit val StringType: Type[String]                   = Type.of[String]
      implicit val OptionStringType: Type[Option[String]]     = Type.of[Option[String]]
      implicit val ListStringType: Type[List[String]]         = Type.of[List[String]]
      implicit val ListOptionType: Type[List[Option[String]]] = Type.of[List[Option[String]]]

      val fields = parameters(caseClass).zipWithIndex.map { case (param, index) => new Field(param, index) }
      fields.find(field => field.transient && !field.param.hasDefault).foreach { field =>
        Environment.reportErrorAndAbort(s"@transientField ${field.param.name} of ${Type[F].prettyPrint} needs a default value")
      }
      val scalaNames = Expr(fields.map(_.param.name))
      val renamed    = Expr(fields.map(_.renamed))

      Expr.quote {
        SprayDerivationRuntime.product[F](Expr.splice(config), Expr.splice(scalaNames), Expr.splice(renamed))(
          (names: SprayDerivationRuntime.Fields, obj: F) =>
            Expr.splice(writeFields(caseClass, fields, Expr.quote(names), Expr.quote(obj))),
          (names: SprayDerivationRuntime.Fields, json: JsValue) =>
            Expr.splice(readFields(caseClass, fields, Expr.quote(names), Expr.quote(json)))
        )
      }
    }

    // Option semantics (missing reads as None, None omitted) unless the user has a format of the Option type itself
    private def optionField[A: Type]: Option[??] = if (existing[A].isDefined) None else optionItem[A]

    private def optionItem[A: Type]: Option[??] = Type[A] match {
      case IsOption(isOption) =>
        import isOption.Underlying as Item
        if (Type[A] =:= Type.of[Option[Item]]) Some(Type[Item].as_??) else None
      case _ => None
    }

    private def writeFields[F: Type](
        caseClass: CaseClass[F],
        fields: List[Field],
        names: Expr[SprayDerivationRuntime.Fields],
        obj: Expr[F]
    ): Expr[List[(String, JsValue)]] = {
      val values = caseClass.caseFieldValuesAt(obj)
      fields.filterNot(_.transient).foldRight(Expr.quote(List.empty[(String, JsValue)])) { (field, rest) =>
        val fieldType: ?? = field.param.tpe
        import fieldType.Underlying as A
        val index = Expr(field.index)
        val value = values.get(field.param.name).map(_.value.asInstanceOf[Expr[A]]).getOrElse {
          // a field not accessible at the call site
          Expr.quote(Expr.splice(obj).asInstanceOf[Product].productElement(Expr.splice(index)).asInstanceOf[A])
        }
        optionField[A] match {
          case Some(item) =>
            import item.Underlying as X
            Expr.quote(
              SprayDerivationRuntime.optionEntry[X](
                Expr.splice(names),
                Expr.splice(index),
                Expr.splice(value.asInstanceOf[Expr[Option[X]]]),
                Expr.splice(format[X]),
                Expr.splice(rest)
              )
            )
          case None =>
            Expr.quote(
              SprayDerivationRuntime.entry[A](
                Expr.splice(names),
                Expr.splice(index),
                Expr.splice(value),
                Expr.splice(format[A]),
                Expr.splice(rest)
              )
            )
        }
      }
    }

    private def readFields[F: Type](
        caseClass: CaseClass[F],
        fields: List[Field],
        names: Expr[SprayDerivationRuntime.Fields],
        json: Expr[JsValue]
    ): Expr[F] = {
      val ordered   = fields.map(field => field.param.name -> argument(field, names, json))
      val arguments = ordered.toMap
      viaCompanionApply[F](arguments)
        .orElse(caseClass.construct[MIO](param => MIO.pure(arguments(param.name))).unsafe.runSync._2 match {
          case Right(Some(constructed)) => Some(constructed)
          case _                        => None
        })
        .orElse(constructViaMirror[F](ordered.map(_._2)))
        .getOrElse(Environment.reportErrorAndAbort(s"${Type[F].prettyPrint} has no accessible constructor or apply"))
    }

    private def argument(field: Field, names: Expr[SprayDerivationRuntime.Fields], json: Expr[JsValue]): Expr_?? = {
      val fieldType: ?? = field.param.tpe
      import fieldType.Underlying as A
      val index          = Expr(field.index)
      val default        = defaultOf[A](field.param)
      val value: Expr[A] =
        if (field.transient) default.get
        else
          optionField[A] match {
            case Some(item) =>
              import item.Underlying as X
              (default match {
                case Some(orElse) =>
                  Expr.quote(
                    SprayDerivationRuntime.readOptionOr[X](
                      Expr.splice(json),
                      Expr.splice(names),
                      Expr.splice(index),
                      Expr.splice(format[X]),
                      Expr.splice(orElse.asInstanceOf[Expr[Option[X]]])
                    )
                  )
                case None =>
                  Expr.quote(
                    SprayDerivationRuntime.readOption[X](
                      Expr.splice(json),
                      Expr.splice(names),
                      Expr.splice(index),
                      Expr.splice(format[X])
                    )
                  )
              }).asInstanceOf[Expr[A]]
            case None =>
              default match {
                case Some(orElse) =>
                  Expr.quote(
                    SprayDerivationRuntime.readOr[A](
                      Expr.splice(json),
                      Expr.splice(names),
                      Expr.splice(index),
                      Expr.splice(format[A]),
                      Expr.splice(orElse)
                    )
                  )
                case None =>
                  Expr.quote(
                    SprayDerivationRuntime.read[A](
                      Expr.splice(json),
                      Expr.splice(names),
                      Expr.splice(index),
                      Expr.splice(format[A])
                    )
                  )
              }
          }
      value.as_??
    }
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

  // companion apply first, so that validating/normalising applys are honoured (as kebs 2 did on Scala 2)
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
    Type[F].unsortedMethodsNamed("apply").iterator.filter(_.isAvailable(AtCallSite)).map(walk).collectFirst { case Some(e) =>
      e
    }
  }
}
