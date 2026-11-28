package pl.iterators.kebs.circe.internal

import hearth.MacroCommons
import hearth.std.StdExtensions
import io.circe.{Decoder, Encoder}
import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.macros.internal.{EntryPointRole, JsonDerivationMacrosImpl}

import scala.annotation.nowarn

private[circe] trait CirceMacrosImpl extends JsonDerivationMacrosImpl { this: MacroCommons & StdExtensions =>

  // how circe takes the entry points' Exported instances up
  private lazy val encoderEntryPoints = methodsNamed(Set("importedEncoder"))(using Type.of[Encoder.type])
  private lazy val decoderEntryPoints = methodsNamed(Set("importedDecoder"))(using Type.of[Decoder.type])
  // not only encodeOption: an Option is also encoded as an Iterable
  private lazy val encoderOptions = implicitsOf(using Type.of[Encoder.type])

  // Exported instances have the lowest priority, so an existing one was preferred already
  private def role[T: Type](typeClass: String, isDerivable: Boolean): EntryPointRole =
    if (isDerivable) entryPointRole[T](typeClass, stepsAsideForExisting = false)(hasInstance = false) else EntryPointRole.StepAside

  def encoderRole[T: Type](config: Expr[JsonConfig]): EntryPointRole = role[T]("Encoder", new EncoderDerivation(config).isDerivable[T])
  def decoderRole[T: Type](config: Expr[JsonConfig]): EntryPointRole = role[T]("Decoder", new DecoderDerivation(config).isDerivable[T])

  // an Encoder.AsObject request derives T although it may have an Encoder, which nested uses of T prefer
  def neededEncoders[T: Type](config: Expr[JsonConfig]): Needed = new EncoderDerivation(config).needed[T](preferExisting = true)
  def neededEncoders[T: Type](config: Expr[JsonConfig], preferExisting: Boolean, hidesExisting: Boolean): Needed =
    new EncoderDerivation(config).needed[T](preferExisting, hidesExisting)
  def recordedEncoder[T: Type](config: Expr[JsonConfig]): Expr[Encoder.AsObject[T]] = asObject(new EncoderDerivation(config).recorded[T])
  def encoderLevel[T: Type](config: Expr[JsonConfig]): Expr[Encoder.AsObject[T]]    = asObject(new EncoderDerivation(config).oneLevel[T])

  def neededDecoders[T: Type](config: Expr[JsonConfig]): Needed = new DecoderDerivation(config).needed[T](preferExisting = false)
  def neededDecoders[T: Type](config: Expr[JsonConfig], preferExisting: Boolean, hidesExisting: Boolean): Needed =
    new DecoderDerivation(config).needed[T](preferExisting, hidesExisting)
  def recordedDecoder[T: Type](config: Expr[JsonConfig]): Expr[Decoder[T]] = new DecoderDerivation(config).recorded[T]
  def decoderLevel[T: Type](config: Expr[JsonConfig]): Expr[Decoder[T]]    = new DecoderDerivation(config).oneLevel[T]

  private def asObject[T: Type](encoder: Expr[Encoder[T]]): Expr[Encoder.AsObject[T]] =
    Expr.quote(CirceDerivationRuntime.asObject(Expr.splice(encoder)))

  // circe has no instances of Either; kebs 2 derived it
  private val derivedStandardLibrary = Set("scala.util.Either", "scala.util.Left", "scala.util.Right")

  def isDerived[T: Type]: Boolean = !isLeftToItsLibrary[T] || derivedStandardLibrary(Type.fqcn[T])

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

  final private class EncoderDerivation(config: Expr[JsonConfig]) extends JsonDerivation[Encoder]("Encoder") {
    override protected def siblings: List[Discoverable]            = List(new DecoderDerivation(config))
    protected def codecType[A: Type]: Type[Encoder[A]]             = Type.of[Encoder[A]]
    protected def entryPoints: Ignored                             = encoderEntryPoints
    protected def libraryContainers: Ignored                       = nothingIgnored
    override protected def libraryOptions: Ignored                 = encoderOptions
    override protected def derivesFromStandardLibrary: Set[String] = derivedStandardLibrary

    @nowarn("msg=never used")
    protected def singletonCodec[T: Type](value: Expr[T]): Expr[Encoder[T]] = Expr.quote(CirceDerivationRuntime.singletonEncoder[T])

    protected def productCodec[T: Type](product: ProductShape[T]): Codecs => Expr[Encoder[T]] = {
      val (scalaNames, renamed) = names(product.fields)
      codecs =>
        Expr.quote {
          CirceDerivationRuntime.productEncoder[T](Expr.splice(config), Expr.splice(scalaNames), Expr.splice(renamed))(
            (members: CirceDerivationRuntime.Members, obj: T) =>
              Expr.splice(writeFields(product, codecs, Expr.quote(members), Expr.quote(obj)))
          )
        }
    }

    private def writeFields[T: Type](
        product: ProductShape[T],
        codecs: Codecs,
        members: Expr[CirceDerivationRuntime.Members],
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

    protected def sumCodec[T: Type](leaves: List[(String, ??)]): Codecs => Expr[Encoder[T]] = {
      val names = caseNames(leaves)
      val tests = caseTests(leaves)
      codecs =>
        Expr.quote {
          CirceDerivationRuntime.sumEncoder[T](Expr.splice(config), Expr.splice(names), Expr.splice(tests), Expr.splice(codecs.local))
        }
    }
  }

  final private class DecoderDerivation(config: Expr[JsonConfig]) extends JsonDerivation[Decoder]("Decoder") {
    override protected def siblings: List[Discoverable]            = List(new EncoderDerivation(config))
    protected def codecType[A: Type]: Type[Decoder[A]]             = Type.of[Decoder[A]]
    protected def entryPoints: Ignored                             = decoderEntryPoints
    protected def libraryContainers: Ignored                       = nothingIgnored
    override protected def derivesFromStandardLibrary: Set[String] = derivedStandardLibrary

    protected def singletonCodec[T: Type](value: Expr[T]): Expr[Decoder[T]] =
      Expr.quote(CirceDerivationRuntime.singletonDecoder[T](Expr.splice(value)))

    // circe decodes an Option itself, a missing one included
    override protected def partsOf[T](product: ProductShape[T]): List[??] = product.fields.filterNot(_.transient).map(_.tpe)

    protected def productCodec[T: Type](product: ProductShape[T]): Codecs => Expr[Decoder[T]] = {
      val (scalaNames, renamed) = names(product.fields)
      codecs =>
        Expr.quote {
          CirceDerivationRuntime.productDecoder[T](Expr.splice(config), Expr.splice(scalaNames), Expr.splice(renamed))(
            Expr.splice(fieldDecoders(product, codecs)),
            (values: Array[Any]) => Expr.splice(construct(product, Expr.quote(values)))
          )
        }
    }

    private def fieldDecoders[T: Type](product: ProductShape[T], codecs: Codecs): Expr[List[CirceDerivationRuntime.FieldDecoder]] = {
      implicit val FieldDecoderType: Type[CirceDerivationRuntime.FieldDecoder] = Type.of[CirceDerivationRuntime.FieldDecoder]
      listOf(product.fields.map(fieldDecoder(_, codecs)))
    }

    private def fieldDecoder(field: JsonField, codecs: Codecs): Expr[CirceDerivationRuntime.FieldDecoder] = {
      val fieldType: ?? = field.tpe
      import fieldType.Underlying as A
      field.default[A] match {
        case Some(default) if field.transient => Expr.quote(CirceDerivationRuntime.transientField[A](Expr.splice(default)))
        case Some(default) => Expr.quote(CirceDerivationRuntime.fieldOr[A](Expr.splice(codecs[A]), Expr.splice(default)))
        case None          => Expr.quote(CirceDerivationRuntime.field[A](Expr.splice(codecs[A])))
      }
    }

    private def construct[T: Type](product: ProductShape[T], values: Expr[Array[Any]]): Expr[T] =
      product.construct(product.fields.map(field => valueAt(field, values)))

    private def valueAt(field: JsonField, values: Expr[Array[Any]]): Expr_?? = {
      val fieldType: ?? = field.tpe
      import fieldType.Underlying as A
      val index = Expr(field.index)
      Expr.quote(CirceDerivationRuntime.value[A](Expr.splice(values), Expr.splice(index))).as_??
    }

    protected def sumCodec[T: Type](leaves: List[(String, ??)]): Codecs => Expr[Decoder[T]] = {
      val names = caseNames(leaves)
      codecs => Expr.quote(CirceDerivationRuntime.sumDecoder[T](Expr.splice(config), Expr.splice(names), Expr.splice(codecs.local)))
    }
  }
}
