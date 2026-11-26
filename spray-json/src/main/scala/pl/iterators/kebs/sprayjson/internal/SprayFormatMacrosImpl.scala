package pl.iterators.kebs.sprayjson.internal

import hearth.MacroCommons
import hearth.std.StdExtensions
import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.macros.internal.{EntryPointRole, JsonDerivationMacrosImpl}
import pl.iterators.kebs.sprayjson.KebsSprayJson
import spray.json.{CollectionFormats, JsValue, JsonFormat, RootJsonFormat, StandardFormats}

private[sprayjson] trait SprayFormatMacrosImpl extends JsonDerivationMacrosImpl { this: MacroCommons & StdExtensions =>

  private def protocolMethods(names: String*): Seq[UntypedMethod] = methodsNamed(names.toSet)(using Type.of[KebsSprayJson])

  private lazy val derivationEntryPoints = protocolMethods("kebsAutoJsonFormat", "kebsAutoJsonFormatSealed")
  // kebs' own generic formats do not count as an existing root instance: a root request derives an object (kebs 2)
  private lazy val ignoredAtRoot = derivationEntryPoints ++ protocolMethods("jsonFlatFormat", "jsonConversionFormat2")
  private lazy val containers    =
    UntypedType.fromTyped(using Type.of[CollectionFormats]).unsortedMethods.filter(_.isImplicit) ++
      methodsNamed(Set("optionFormat"))(using Type.of[StandardFormats])

  private def existing[T: Type]: Option[Expr[JsonFormat[T]]] = {
    implicit val JsonFormatT: Type[JsonFormat[T]] = Type.of[JsonFormat[T]]
    summonInstance[JsonFormat[T]](ignoredAtRoot)
  }

  def formatRole[T: Type](stepsAsideForExisting: Boolean): EntryPointRole =
    entryPointRole[T]("JsonFormat", stepsAsideForExisting)(existing[T].isDefined)

  def isDerived[T: Type](config: Expr[JsonConfig]): Boolean = !isLeftToItsLibrary[T] || new SprayDerivation(config).isDerivable[T]

  def existingFormat[T: Type]: Expr[RootJsonFormat[T]] =
    asRoot(existing[T].getOrElse(Environment.reportErrorAndAbort(s"No JsonFormat of ${Type[T].prettyPrint}")))

  // an explicit derivation leaves nested uses of T to another instance of it, if there is one
  def neededFormats[T: Type](config: Expr[JsonConfig], preferExisting: Boolean, hidesExisting: Boolean = false): Needed =
    new SprayDerivation(config).needed[T](preferExisting, hidesExisting)

  def neededBesideExisting[T: Type](config: Expr[JsonConfig]): Needed = new SprayDerivation(config).neededBeside[T](ignoredAtRoot)

  def recordedFormat[T: Type](config: Expr[JsonConfig]): Expr[RootJsonFormat[T]] = asRoot(new SprayDerivation(config).recorded[T])

  def formatLevel[T: Type](config: Expr[JsonConfig]): Expr[RootJsonFormat[T]] = asRoot(new SprayDerivation(config).oneLevel[T])

  private def asRoot[T: Type](format: Expr[JsonFormat[T]]): Expr[RootJsonFormat[T]] =
    Expr.quote(SprayDerivationRuntime.asRoot(Expr.splice(format)))

  final private class SprayDerivation(config: Expr[JsonConfig]) extends JsonDerivation[JsonFormat]("JsonFormat") {
    protected def codecType[A: Type]: Type[JsonFormat[A]] = Type.of[JsonFormat[A]]
    protected def entryPoints: Seq[UntypedMethod]         = derivationEntryPoints
    // spray has no formats of these; kebs 2 derived them
    override protected def derivesFromStandardLibrary: Set[String] = Set("scala.Some", "scala.util.Left", "scala.util.Right")
    protected def libraryContainers: Seq[UntypedMethod]            = containers

    protected def singletonCodec[T: Type](value: Expr[T]): Expr[JsonFormat[T]] =
      Expr.quote(SprayDerivationRuntime.singleton[T](Expr.splice(value)))

    protected def productCodec[T: Type](product: ProductShape[T]): Codecs => Expr[JsonFormat[T]] = {
      implicit val StringType: Type[String]                   = Type.of[String]
      implicit val OptionStringType: Type[Option[String]]     = Type.of[Option[String]]
      implicit val ListStringType: Type[List[String]]         = Type.of[List[String]]
      implicit val ListOptionType: Type[List[Option[String]]] = Type.of[List[Option[String]]]
      val scalaNames                                          = Expr(product.fields.map(_.name))
      val renamed                                             = Expr(product.fields.map(_.renamed))
      codecs =>
        Expr.quote {
          SprayDerivationRuntime.product[T](Expr.splice(config), Expr.splice(scalaNames), Expr.splice(renamed))(
            (members: SprayDerivationRuntime.Members, obj: T) =>
              Expr.splice(writeFields(product, codecs, Expr.quote(members), Expr.quote(obj))),
            (names: SprayDerivationRuntime.Fields, json: JsValue) =>
              Expr.splice(readFields(product, codecs, Expr.quote(names), Expr.quote(json)))
          )
        }
    }

    private def writeFields[T: Type](
        product: ProductShape[T],
        codecs: Codecs,
        members: Expr[SprayDerivationRuntime.Members],
        obj: Expr[T]
    ): Expr[Unit] =
      combined(product.fields.filterNot(_.transient).map { field =>
        val fieldType: ?? = field.tpe
        import fieldType.Underlying as A
        val index = Expr(field.index)
        val value = product.value[A](obj, field)
        product.optionItem(field) match {
          case Some(item) =>
            import item.Underlying as X
            Expr.quote {
              Expr
                .splice(members)
                .option[X](Expr.splice(index), Expr.splice(value.asInstanceOf[Expr[Option[X]]]), Expr.splice(codecs[X]))
            }
          case None => Expr.quote(Expr.splice(members).add[A](Expr.splice(index), Expr.splice(value), Expr.splice(codecs[A])))
        }
      }) { (first, second) =>
        Expr.quote {
          Expr.splice(first)
          Expr.splice(second)
        }
      }.getOrElse(Expr.quote(()))

    private def readFields[T: Type](
        product: ProductShape[T],
        codecs: Codecs,
        names: Expr[SprayDerivationRuntime.Fields],
        json: Expr[JsValue]
    ): Expr[T] =
      product.construct(product.fields.map(field => argument(product, codecs, field, names, json)))

    private def argument[T: Type](
        product: ProductShape[T],
        codecs: Codecs,
        field: JsonField,
        names: Expr[SprayDerivationRuntime.Fields],
        json: Expr[JsValue]
    ): Expr_?? = {
      val fieldType: ?? = field.tpe
      import fieldType.Underlying as A
      val index          = Expr(field.index)
      val default        = field.default[A]
      val value: Expr[A] =
        if (field.transient) default.get
        else
          product.optionItem(field) match {
            case Some(item) =>
              import item.Underlying as X
              (default match {
                case Some(orElse) =>
                  Expr.quote(
                    SprayDerivationRuntime.readOptionOr[X](
                      Expr.splice(json),
                      Expr.splice(names),
                      Expr.splice(index),
                      Expr.splice(codecs[X]),
                      Expr.splice(orElse.asInstanceOf[Expr[Option[X]]])
                    )
                  )
                case None =>
                  Expr.quote(
                    SprayDerivationRuntime.readOption[X](Expr.splice(json), Expr.splice(names), Expr.splice(index), Expr.splice(codecs[X]))
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
                      Expr.splice(codecs[A]),
                      Expr.splice(orElse)
                    )
                  )
                case None =>
                  Expr.quote(
                    SprayDerivationRuntime.read[A](Expr.splice(json), Expr.splice(names), Expr.splice(index), Expr.splice(codecs[A]))
                  )
              }
          }
      value.as_??
    }

    protected def sumCodec[T: Type](leaves: List[(String, ??)]): Codecs => Expr[JsonFormat[T]] = {
      implicit val StringType: Type[String]           = Type.of[String]
      implicit val ListStringType: Type[List[String]] = Type.of[List[String]]
      val caseNames                                   = Expr(leaves.map(_._1))
      val tests                                       = caseTests(leaves)
      codecs =>
        Expr.quote {
          SprayDerivationRuntime.sum[T](Expr.splice(config), Expr.splice(caseNames), Expr.splice(tests), Expr.splice(codecs.local))
        }
    }
  }
}
