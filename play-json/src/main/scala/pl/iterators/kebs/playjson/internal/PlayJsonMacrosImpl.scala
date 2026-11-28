package pl.iterators.kebs.playjson.internal

import hearth.MacroCommons
import hearth.std.StdExtensions
import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.macros.internal.{EntryPointRole, JsonDerivationMacrosImpl}
import pl.iterators.kebs.playjson.KebsPlayJson
import play.api.libs.json._

import scala.annotation.nowarn

private[playjson] trait PlayJsonMacrosImpl extends JsonDerivationMacrosImpl { this: MacroCommons & StdExtensions =>

  private lazy val readsEntryPoints  = methodsNamed(Set("kebsAutoReads", "kebsAutoReadsSealed"))(using Type.of[KebsPlayJson])
  private lazy val writesEntryPoints = methodsNamed(Set("kebsAutoWrites", "kebsAutoWritesSealed"))(using Type.of[KebsPlayJson])

  private lazy val readsContainers =
    methodsNamed(Set("ArrayReads", "charMapReads", "mapReads"))(using Type.of[DefaultReads]) ++
      methodsNamed(Set("keyMapReads", "traversableReads"))(using Type.of[LowPriorityDefaultReads])

  private lazy val iterableWrites =
    methodsNamed(Set("iterableWrites", "iterableWrites2", "traversableWrites"))(using Type.of[LowPriorityWrites])

  private lazy val writesContainers = iterableWrites ++
    methodsNamed(Set("arrayWrites", "genericMapWrites", "keyMapWrites", "mapWrites", "OptionWrites", "someWrites"))(using
      Type.of[DefaultWrites]
    )

  private def existingInstance[T: Type]: Option[Expr[Reads[T]]] = {
    implicit val ReadsT: Type[Reads[T]] = Type.of[Reads[T]]
    summonInstance[Reads[T]](readsEntryPoints)
  }

  def readsRole[T: Type](stepsAsideForExisting: Boolean): EntryPointRole =
    entryPointRole[T]("Reads", stepsAsideForExisting)(existingInstance[T].isDefined)

  def writesRole[T: Type]: EntryPointRole = {
    implicit val WritesT: Type[Writes[T]] = Type.of[Writes[T]]
    entryPointRole[T]("Writes", stepsAsideForExisting = true)(summonInstance[Writes[T]](writesEntryPoints).isDefined)
  }

  def existingReads[T: Type]: Expr[Reads[T]] =
    existingInstance[T].getOrElse(Environment.reportErrorAndAbort(s"No Reads of ${Type[T].prettyPrint}"))

  // an explicit derivation leaves nested uses of T to another instance of it, if there is one
  def neededReads[T: Type](config: Expr[JsonConfig], preferExisting: Boolean, hidesExisting: Boolean = false): Needed =
    new ReadsDerivation(config).needed[T](preferExisting, hidesExisting)
  def neededReadsBesideExisting[T: Type](config: Expr[JsonConfig]): Needed = new ReadsDerivation(config).neededBeside[T](readsEntryPoints)
  def recordedReads[T: Type](config: Expr[JsonConfig]): Expr[Reads[T]]     = new ReadsDerivation(config).recorded[T]
  def readsLevel[T: Type](config: Expr[JsonConfig]): Expr[Reads[T]]        = new ReadsDerivation(config).oneLevel[T]

  def neededWrites[T: Type](config: Expr[JsonConfig], preferExisting: Boolean, hidesExisting: Boolean = false): Needed =
    new WritesDerivation(config).needed[T](preferExisting, hidesExisting)
  def recordedWrites[T: Type](config: Expr[JsonConfig]): Expr[OWrites[T]]       = asOWrites(new WritesDerivation(config).recorded[T])
  def writesLevel[T: Type](config: Expr[JsonConfig]): Expr[OWrites[T]]          = asOWrites(collectionWritesLevel[T](config))
  def collectionWritesLevel[T: Type](config: Expr[JsonConfig]): Expr[Writes[T]] = new WritesDerivation(config).oneLevel[T]

  private def asOWrites[T: Type](writes: Expr[Writes[T]]): Expr[OWrites[T]] =
    Expr.quote(PlayDerivationRuntime.asOWrites(Expr.splice(writes)))

  private def names(fields: List[JsonField]): (Expr[List[String]], Expr[List[Option[String]]]) = {
    implicit val StringType: Type[String]                   = Type.of[String]
    implicit val OptionStringType: Type[Option[String]]     = Type.of[Option[String]]
    implicit val ListStringType: Type[List[String]]         = Type.of[List[String]]
    implicit val ListOptionType: Type[List[Option[String]]] = Type.of[List[Option[String]]]
    (Expr(fields.map(_.name)), Expr(fields.map(_.renamed)))
  }

  private def caseNames(leaves: List[(String, ??)]): Expr[List[String]] = {
    implicit val StringType: Type[String]           = Type.of[String]
    implicit val ListStringType: Type[List[String]] = Type.of[List[String]]
    Expr(leaves.map(_._1))
  }

  final private class ReadsDerivation(config: Expr[JsonConfig]) extends JsonDerivation[Reads]("Reads") {
    override protected def siblings: List[Discoverable] = List(new WritesDerivation(config))
    protected def codecType[A: Type]: Type[Reads[A]]    = Type.of[Reads[A]]
    protected def entryPoints: Ignored                  = readsEntryPoints
    protected def libraryContainers: Ignored            = readsContainers

    protected def singletonCodec[T: Type](value: Expr[T]): Expr[Reads[T]] =
      Expr.quote(PlayDerivationRuntime.singletonReads[T](Expr.splice(value)))

    protected def productCodec[T: Type](product: ProductShape[T]): Codecs => Expr[Reads[T]] = {
      val (scalaNames, renamed) = names(product.fields)
      codecs =>
        Expr.quote {
          PlayDerivationRuntime.productReads[T](Expr.splice(config), Expr.splice(scalaNames), Expr.splice(renamed))(
            (names: PlayDerivationRuntime.Fields, json: JsValue) =>
              Expr.splice(readFields(product, codecs, Expr.quote(names), Expr.quote(json))),
            (values: Array[Any]) => Expr.splice(construct(product, Expr.quote(values)))
          )
        }
    }

    private def readFields[T: Type](
        product: ProductShape[T],
        codecs: Codecs,
        names: Expr[PlayDerivationRuntime.Fields],
        json: Expr[JsValue]
    ): Expr[List[JsResult[Any]]] = {
      implicit val ResultType: Type[JsResult[Any]] = Type.of[JsResult[Any]]
      listOf(product.fields.map(field => fieldResult(product, codecs, field, names, json)))
    }

    private def construct[T: Type](product: ProductShape[T], values: Expr[Array[Any]]): Expr[T] =
      product.construct(product.fields.map(field => valueAt(field, values)))

    private def valueAt(field: JsonField, values: Expr[Array[Any]]): Expr_?? = {
      val fieldType: ?? = field.tpe
      import fieldType.Underlying as A
      val index = Expr(field.index)
      Expr.quote(PlayDerivationRuntime.value[A](Expr.splice(values), Expr.splice(index))).as_??
    }

    private def fieldResult[T: Type](
        product: ProductShape[T],
        codecs: Codecs,
        field: JsonField,
        names: Expr[PlayDerivationRuntime.Fields],
        json: Expr[JsValue]
    ): Expr[JsResult[Any]] = {
      val fieldType: ?? = field.tpe
      import fieldType.Underlying as A
      val index   = Expr(field.index)
      val default = field.default[A]
      if (field.transient) Expr.quote(JsSuccess(Expr.splice(default.get)))
      else
        product.optionItem(field) match {
          case Some(item) =>
            import item.Underlying as X
            default match {
              case Some(orElse) =>
                Expr.quote(
                  PlayDerivationRuntime.optionFieldOr[X](
                    Expr.splice(json),
                    Expr.splice(names),
                    Expr.splice(index),
                    Expr.splice(codecs[X]),
                    Expr.splice(orElse.asInstanceOf[Expr[Option[X]]])
                  )
                )
              case None =>
                Expr.quote(
                  PlayDerivationRuntime.optionField[X](Expr.splice(json), Expr.splice(names), Expr.splice(index), Expr.splice(codecs[X]))
                )
            }
          case None =>
            default match {
              case Some(orElse) =>
                Expr.quote(
                  PlayDerivationRuntime.fieldOr[A](
                    Expr.splice(json),
                    Expr.splice(names),
                    Expr.splice(index),
                    Expr.splice(codecs[A]),
                    Expr.splice(orElse)
                  )
                )
              case None =>
                Expr.quote(
                  PlayDerivationRuntime.field[A](Expr.splice(json), Expr.splice(names), Expr.splice(index), Expr.splice(codecs[A]))
                )
            }
        }
    }

    protected def sumCodec[T: Type](leaves: List[(String, ??)]): Codecs => Expr[Reads[T]] = {
      val names = caseNames(leaves)
      codecs => Expr.quote(PlayDerivationRuntime.sumReads[T](Expr.splice(config), Expr.splice(names), Expr.splice(codecs.local)))
    }
  }

  final private class WritesDerivation(config: Expr[JsonConfig]) extends JsonDerivation[Writes]("Writes") {
    override protected def siblings: List[Discoverable] = List(new ReadsDerivation(config))
    protected def codecType[A: Type]: Type[Writes[A]]   = Type.of[Writes[A]]
    protected def entryPoints: Ignored                  = writesEntryPoints
    protected def libraryContainers: Ignored            = writesContainers
    override protected def libraryCollections: Ignored  = iterableWrites

    // iterableWrites2 accepts the Writes of any supertype of the elements, which on Scala 3 can be a local one
    override protected def elementOf[F: Type]: Option[??] = Type.typeArguments[F] match {
      case List(element) if isStandardLibrary[F] || !Type.isCaseClass[F] =>
        import element.Underlying as X
        if (Type[F] <:< Type.of[Iterable[X]]) Some(element) else None
      case _ => None
    }

    override protected def collectionCodec[F: Type, X: Type](codecs: Codecs): Expr[Writes[F]] =
      Expr.quote(PlayDerivationRuntime.collectionWrites[F, X](Expr.splice(codecs[X])))

    @nowarn("msg=never used")
    protected def singletonCodec[T: Type](value: Expr[T]): Expr[Writes[T]] =
      Expr.quote(PlayDerivationRuntime.singletonWrites[T])

    protected def productCodec[T: Type](product: ProductShape[T]): Codecs => Expr[Writes[T]] = {
      val (scalaNames, renamed) = names(product.fields)
      codecs =>
        Expr.quote {
          PlayDerivationRuntime.productWrites[T](Expr.splice(config), Expr.splice(scalaNames), Expr.splice(renamed))(
            (members: PlayDerivationRuntime.Members, obj: T) =>
              Expr.splice(writeFields(product, codecs, Expr.quote(members), Expr.quote(obj)))
          )
        }
    }

    private def writeFields[T: Type](
        product: ProductShape[T],
        codecs: Codecs,
        members: Expr[PlayDerivationRuntime.Members],
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

    protected def sumCodec[T: Type](leaves: List[(String, ??)]): Codecs => Expr[Writes[T]] = {
      val names = caseNames(leaves)
      val tests = caseTests(leaves)
      codecs =>
        Expr.quote {
          PlayDerivationRuntime.sumWrites[T](Expr.splice(config), Expr.splice(names), Expr.splice(tests), Expr.splice(codecs.local))
        }
    }
  }
}
