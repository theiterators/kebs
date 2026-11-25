package pl.iterators.kebs.jsoniter.internal

import com.github.plokhotnyuk.jsoniter_scala.core.{JsonCodec, JsonKeyCodec, JsonValueCodec, JsonWriter}
import hearth.MacroCommons
import hearth.std.StdExtensions
import pl.iterators.kebs.core.instances.InstanceConverter
import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.macros.ValueClassLike
import pl.iterators.kebs.core.macros.internal.{EntryPointRole, JsonDerivationMacrosImpl, JsonDerivationState}
import pl.iterators.kebs.jsoniter.KebsJsoniter

import java.time._
import java.util.UUID
import scala.collection.{Factory, mutable}
import scala.reflect.ClassTag

private[jsoniter] trait JsoniterMacrosImpl extends JsonDerivationMacrosImpl { this: MacroCommons & StdExtensions =>

  private lazy val derivationEntryPoints = methodsNamed(Set("kebsAutoJsonValueCodec"))(using Type.of[KebsJsoniter])

  def entryRole[T: Type](config: Expr[JsonConfig]): EntryPointRole =
    if (isBottom[T] || !JsonDerivationState.isDiscovering && !isOffered[T]) EntryPointRole.StepAside
    else {
      implicit val CodecT: Type[JsonValueCodec[T]] = Type.of[JsonValueCodec[T]]
      val derivation                               = new JsoniterDerivation(config)
      if (!derivation.isDerivable[T] && !derivation.isInline[T]) EntryPointRole.StepAside
      else
        entryPointRole[T]("JsonValueCodec", stepsAsideForExisting = true) {
          summonInstance[JsonValueCodec[T]](derivationEntryPoints).isDefined
        }
    }

  private def isBottom[T: Type]: Boolean = Type[T] =:= Type.of[Nothing] || Type[T] =:= Type.of[Null]

  // not what other libraries send in their own way (strings, options, pekko's types), nor a case object: its own type is rarely
  // meant, and is written differently
  private def isOffered[T: Type]: Boolean = {
    val name = Type.fqcn[T]
    if (name.startsWith("org.apache.pekko.") || name.startsWith("akka.")) false
    else if (isStandardLibrary[T]) isOfferedStandardType[T] || valueClassOf[T].isDefined || converterOf[T].isDefined
    else Expr.singletonOf[T].isEmpty
  }

  private def isOfferedStandardType[T: Type]: Boolean = Type[T] match {
    case ArrayOf(item) =>
      import item.Underlying as X
      !(Type[X] =:= Type.of[Byte]) && !(Type[X] =:= Type.of[Char])
    case IterableOf(_) => true
    case _             => false
  }

  def recordedCodec[T: Type](config: Expr[JsonConfig]): Expr[JsonValueCodec[T]] = new JsoniterDerivation(config).recorded[T]

  // an explicit derivation leaves nested uses of T to another instance of it, if there is one
  def neededCodecs[T: Type](config: Expr[JsonConfig], preferExisting: Boolean, hidesExisting: Boolean = false): Needed =
    new JsoniterDerivation(config).needed[T](preferExisting, hidesExisting)

  def codecLevel[T: Type](config: Expr[JsonConfig]): Expr[JsonValueCodec[T]] = new JsoniterDerivation(config).oneLevel[T]

  final private class Standard[A](codec: => Expr[JsonCodec[A]])(implicit A: Type[A]) {
    def codecOf[F: Type]: Option[Expr[JsonCodec[F]]] = if (Type[F] =:= A) Some(codec.asInstanceOf[Expr[JsonCodec[F]]]) else None
  }

  private lazy val standardTypes: Map[String, Standard[?]] = {
    implicit val BooleanType: Type[Boolean]               = Type.of[Boolean]
    implicit val ByteType: Type[Byte]                     = Type.of[Byte]
    implicit val ShortType: Type[Short]                   = Type.of[Short]
    implicit val IntType: Type[Int]                       = Type.of[Int]
    implicit val LongType: Type[Long]                     = Type.of[Long]
    implicit val FloatType: Type[Float]                   = Type.of[Float]
    implicit val DoubleType: Type[Double]                 = Type.of[Double]
    implicit val CharType: Type[Char]                     = Type.of[Char]
    implicit val StringType: Type[String]                 = Type.of[String]
    implicit val BigIntType: Type[BigInt]                 = Type.of[BigInt]
    implicit val BigDecimalType: Type[BigDecimal]         = Type.of[BigDecimal]
    implicit val UUIDType: Type[UUID]                     = Type.of[UUID]
    implicit val DurationType: Type[Duration]             = Type.of[Duration]
    implicit val InstantType: Type[Instant]               = Type.of[Instant]
    implicit val LocalDateType: Type[LocalDate]           = Type.of[LocalDate]
    implicit val LocalDateTimeType: Type[LocalDateTime]   = Type.of[LocalDateTime]
    implicit val LocalTimeType: Type[LocalTime]           = Type.of[LocalTime]
    implicit val MonthDayType: Type[MonthDay]             = Type.of[MonthDay]
    implicit val OffsetDateTimeType: Type[OffsetDateTime] = Type.of[OffsetDateTime]
    implicit val OffsetTimeType: Type[OffsetTime]         = Type.of[OffsetTime]
    implicit val PeriodType: Type[Period]                 = Type.of[Period]
    implicit val YearType: Type[Year]                     = Type.of[Year]
    implicit val YearMonthType: Type[YearMonth]           = Type.of[YearMonth]
    implicit val ZonedDateTimeType: Type[ZonedDateTime]   = Type.of[ZonedDateTime]
    implicit val ZoneIdType: Type[ZoneId]                 = Type.of[ZoneId]
    implicit val ZoneOffsetType: Type[ZoneOffset]         = Type.of[ZoneOffset]
    Map(
      "scala.Boolean"            -> new Standard[Boolean](Expr.quote(JsoniterCodecs.boolean)),
      "scala.Byte"               -> new Standard[Byte](Expr.quote(JsoniterCodecs.byte)),
      "scala.Short"              -> new Standard[Short](Expr.quote(JsoniterCodecs.short)),
      "scala.Int"                -> new Standard[Int](Expr.quote(JsoniterCodecs.int)),
      "scala.Long"               -> new Standard[Long](Expr.quote(JsoniterCodecs.long)),
      "scala.Float"              -> new Standard[Float](Expr.quote(JsoniterCodecs.float)),
      "scala.Double"             -> new Standard[Double](Expr.quote(JsoniterCodecs.double)),
      "scala.Char"               -> new Standard[Char](Expr.quote(JsoniterCodecs.char)),
      "java.lang.String"         -> new Standard[String](Expr.quote(JsoniterCodecs.string)),
      "scala.math.BigInt"        -> new Standard[BigInt](Expr.quote(JsoniterCodecs.bigInt)),
      "scala.math.BigDecimal"    -> new Standard[BigDecimal](Expr.quote(JsoniterCodecs.bigDecimal)),
      "java.util.UUID"           -> new Standard[UUID](Expr.quote(JsoniterCodecs.uuid)),
      "java.time.Duration"       -> new Standard[Duration](Expr.quote(JsoniterCodecs.duration)),
      "java.time.Instant"        -> new Standard[Instant](Expr.quote(JsoniterCodecs.instant)),
      "java.time.LocalDate"      -> new Standard[LocalDate](Expr.quote(JsoniterCodecs.localDate)),
      "java.time.LocalDateTime"  -> new Standard[LocalDateTime](Expr.quote(JsoniterCodecs.localDateTime)),
      "java.time.LocalTime"      -> new Standard[LocalTime](Expr.quote(JsoniterCodecs.localTime)),
      "java.time.MonthDay"       -> new Standard[MonthDay](Expr.quote(JsoniterCodecs.monthDay)),
      "java.time.OffsetDateTime" -> new Standard[OffsetDateTime](Expr.quote(JsoniterCodecs.offsetDateTime)),
      "java.time.OffsetTime"     -> new Standard[OffsetTime](Expr.quote(JsoniterCodecs.offsetTime)),
      "java.time.Period"         -> new Standard[Period](Expr.quote(JsoniterCodecs.period)),
      "java.time.Year"           -> new Standard[Year](Expr.quote(JsoniterCodecs.year)),
      "java.time.YearMonth"      -> new Standard[YearMonth](Expr.quote(JsoniterCodecs.yearMonth)),
      "java.time.ZonedDateTime"  -> new Standard[ZonedDateTime](Expr.quote(JsoniterCodecs.zonedDateTime)),
      "java.time.ZoneId"         -> new Standard[ZoneId](Expr.quote(JsoniterCodecs.zoneId)),
      "java.time.ZoneOffset"     -> new Standard[ZoneOffset](Expr.quote(JsoniterCodecs.zoneOffset))
    )
  }

  private def standardCodec[F: Type]: Option[Expr[JsonCodec[F]]] = standardTypes.get(Type.fqcn[F]).flatMap(_.codecOf[F])

  private lazy val OptionOf   = Type.Ctor1.of[Option]
  private lazy val ArrayOf    = Type.Ctor1.of[Array]
  private lazy val IterableOf = Type.Ctor1.of[Iterable]
  private lazy val MapOf      = Type.Ctor2.of[scala.collection.Map]

  final private class JsoniterDerivation(config: Expr[JsonConfig]) extends JsonDerivation[JsonValueCodec]("JsonValueCodec") {
    protected def codecType[A: Type]: Type[JsonValueCodec[A]]           = Type.of[JsonValueCodec[A]]
    protected def entryPoints: Seq[UntypedMethod]                       = derivationEntryPoints
    protected def libraryContainers: Seq[UntypedMethod]                 = Nil
    private val composites: mutable.Map[String, Option[Composite[Any]]] = mutable.Map.empty

    override protected def composite[F: Type]: Option[Composite[F]] =
      composites
        .getOrElseUpdate(
          Type[F].plainPrint,
          (if (isBottom[F]) None else wrapper[F].orElse(standard[F])).asInstanceOf[Option[Composite[Any]]]
        )
        .asInstanceOf[Option[Composite[F]]]

    private def wrapper[F: Type]: Option[Composite[F]] =
      converterOf[F]
        .map { converter =>
          val base: ?? = converter.base
          import base.Underlying as B
          val instance = converter.instance.asTyped[InstanceConverter[F, B]](using Type.of[InstanceConverter[F, B]])
          composed[F, B](codecs => Expr.quote(JsoniterDerivationRuntime.converted[F, B](Expr.splice(codecs.local), Expr.splice(instance))))
        }
        .orElse(valueClassOf[F].map { valueClass =>
          val base: ?? = valueClass.base
          import base.Underlying as B
          val instance = valueClass.instance.asTyped[ValueClassLike[F, B]](using Type.of[ValueClassLike[F, B]])
          composed[F, B](codecs =>
            Expr.quote(JsoniterDerivationRuntime.valueClass[F, B](Expr.splice(codecs.local), Expr.splice(instance)))
          )
        })

    private def standard[F: Type]: Option[Composite[F]] =
      standardCodec[F]
        .map(codec => new Composite[F](Nil, _ => codec.asInstanceOf[Expr[JsonValueCodec[F]]]))
        .orElse(container[F])
        .orElse(tuple[F])

    private def container[F: Type]: Option[Composite[F]] = Type[F] match {
      case OptionOf(item) =>
        import item.Underlying as X
        if (Type[F] =:= Type.of[Option[X]])
          Some(
            composed[F, X](codecs =>
              Expr.quote(JsoniterDerivationRuntime.option[X](Expr.splice(codecs.local))).asInstanceOf[Expr[JsonValueCodec[F]]]
            )
          )
        else None
      case ArrayOf(item) =>
        import item.Underlying as X
        implicit val ClassTagX: Type[ClassTag[X]] = Type.of[ClassTag[X]]
        summonInstance[ClassTag[X]](Nil).map { tag =>
          composed[F, X](codecs =>
            Expr
              .quote(JsoniterDerivationRuntime.array[X](Expr.splice(codecs.local), Expr.splice(tag)))
              .asInstanceOf[Expr[JsonValueCodec[F]]]
          )
        }
      case MapOf(key, value) =>
        import key.Underlying as K
        import value.Underlying as V
        implicit val FactoryF: Type[Factory[(K, V), F]] = Type.of[Factory[(K, V), F]]
        for {
          keys    <- keyCodec[K]
          factory <- summonInstance[Factory[(K, V), F]](Nil)
        } yield composed[F, V](codecs =>
          Expr.quote(JsoniterDerivationRuntime.map[K, V, F](Expr.splice(codecs.local), Expr.splice(keys), Expr.splice(factory)))
        )
      case IterableOf(item) =>
        import item.Underlying as X
        implicit val FactoryF: Type[Factory[X, F]] = Type.of[Factory[X, F]]
        summonInstance[Factory[X, F]](Nil).map { factory =>
          composed[F, X](codecs => Expr.quote(JsoniterDerivationRuntime.iterable[X, F](Expr.splice(codecs.local), Expr.splice(factory))))
        }
      case _ => None
    }

    private def composed[F: Type, X: Type](build: Codecs => Expr[JsonValueCodec[F]]): Composite[F] =
      new Composite[F](List(Type[X].as_??), build)

    private def keyCodec[K: Type]: Option[Expr[JsonKeyCodec[K]]] = {
      implicit val KeyCodec: Type[JsonKeyCodec[K]] = Type.of[JsonKeyCodec[K]]
      summonInstance[JsonKeyCodec[K]](Nil)
        .orElse(converterOf[K].flatMap { converter =>
          val base: ?? = converter.base
          import base.Underlying as B
          val instance = converter.instance.asTyped[InstanceConverter[K, B]](using Type.of[InstanceConverter[K, B]])
          keyCodec[B].map(key => Expr.quote(JsoniterDerivationRuntime.convertedKey[K, B](Expr.splice(key), Expr.splice(instance))))
        })
        .orElse(valueClassOf[K].flatMap { valueClass =>
          val base: ?? = valueClass.base
          import base.Underlying as B
          val instance = valueClass.instance.asTyped[ValueClassLike[K, B]](using Type.of[ValueClassLike[K, B]])
          keyCodec[B].map(key => Expr.quote(JsoniterDerivationRuntime.valueClassKey[K, B](Expr.splice(key), Expr.splice(instance))))
        })
        .orElse(standardCodec[K].map(_.asInstanceOf[Expr[JsonKeyCodec[K]]]))
    }

    private def tuple[F: Type]: Option[Composite[F]] = {
      val name = Type.fqcn[F]
      if (name.startsWith("scala.Tuple") || name.startsWith("("))
        CaseClass.parse[F].toOption.map { caseClass =>
          val product = new ProductShape[F](caseClass)
          val parts   = product.fields.map(_.tpe)
          val names   = parts.map(_.Underlying.plainPrint)
          // which codec an element has
          val indices = intList(names.map(names.distinct.indexOf(_)))
          new Composite[F](
            parts,
            codecs =>
              Expr.quote {
                JsoniterDerivationRuntime.tuple[F](Expr.splice(codecs.local), Expr.splice(indices))((values: Array[Any]) =>
                  Expr.splice(construct(product, Expr.quote(values)))
                )
              }
          )
        }
      else None
    }

    private def intList(values: List[Int]): Expr[List[Int]] = {
      implicit val IntType: Type[Int]           = Type.of[Int]
      implicit val ListIntType: Type[List[Int]] = Type.of[List[Int]]
      Expr(values)
    }

    private def construct[T: Type](product: ProductShape[T], values: Expr[Array[Any]]): Expr[T] =
      product.construct(product.fields.map { field =>
        val fieldType: ?? = field.tpe
        import fieldType.Underlying as A
        val index = Expr(field.index)
        Expr.quote(JsoniterDerivationRuntime.value[A](Expr.splice(values), Expr.splice(index))).as_??
      })

    protected def singletonCodec[T: Type](value: Expr[T]): Expr[JsonValueCodec[T]] =
      Expr.quote(JsoniterDerivationRuntime.singleton[T](Expr.splice(value)))

    protected def productCodec[T: Type](product: ProductShape[T]): Codecs => Expr[JsonValueCodec[T]] = {
      implicit val StringType: Type[String]                     = Type.of[String]
      implicit val OptionStringType: Type[Option[String]]       = Type.of[Option[String]]
      implicit val ListStringType: Type[List[String]]           = Type.of[List[String]]
      implicit val ListOptionType: Type[List[Option[String]]]   = Type.of[List[Option[String]]]
      implicit val Field: Type[JsoniterDerivationRuntime.Field] = Type.of[JsoniterDerivationRuntime.Field]
      val scalaNames                                            = Expr(product.fields.map(_.name))
      val renamed                                               = Expr(product.fields.map(_.renamed))
      codecs => {
        val fields = listOf(product.fields.map(readField(product, codecs, _)))
        Expr.quote {
          JsoniterDerivationRuntime.product[T](
            Expr.splice(config),
            Expr.splice(scalaNames),
            Expr.splice(renamed),
            Expr.splice(fields)
          )(
            (fields: JsoniterDerivationRuntime.Fields, out: JsonWriter, obj: T) =>
              Expr.splice(writeFields(product, codecs, Expr.quote(fields), Expr.quote(out), Expr.quote(obj))),
            (values: Array[Any]) => Expr.splice(construct(product, Expr.quote(values)))
          )
        }
      }
    }

    private def readField[T: Type](
        product: ProductShape[T],
        codecs: Codecs,
        field: JsonField
    ): Expr[JsoniterDerivationRuntime.Field] = {
      val fieldType: ?? = field.tpe
      import fieldType.Underlying as A
      val default = field.default[A]
      if (field.transient) {
        val value = default.get
        Expr.quote(JsoniterDerivationRuntime.ignored(Expr.splice(value)))
      } else
        product.optionItem(field) match {
          case Some(item) =>
            import item.Underlying as X
            val index = Expr(codecs.indexOf[X])
            default match {
              case Some(value) =>
                Expr.quote(JsoniterDerivationRuntime.optionalOr(Expr.splice(codecs.local), Expr.splice(index), Expr.splice(value)))
              case None => Expr.quote(JsoniterDerivationRuntime.optional(Expr.splice(codecs.local), Expr.splice(index)))
            }
          case None =>
            val index = Expr(codecs.indexOf[A])
            default match {
              case Some(value) =>
                Expr.quote(JsoniterDerivationRuntime.requiredOr(Expr.splice(codecs.local), Expr.splice(index), Expr.splice(value)))
              case None => Expr.quote(JsoniterDerivationRuntime.required(Expr.splice(codecs.local), Expr.splice(index)))
            }
        }
    }

    private def writeFields[T: Type](
        product: ProductShape[T],
        codecs: Codecs,
        fields: Expr[JsoniterDerivationRuntime.Fields],
        out: Expr[JsonWriter],
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
              JsoniterDerivationRuntime.writeOption[X](
                Expr.splice(fields),
                Expr.splice(out),
                Expr.splice(index),
                Expr.splice(value.asInstanceOf[Expr[Option[X]]]),
                Expr.splice(codecs[X])
              )
            }
          case None =>
            Expr.quote {
              JsoniterDerivationRuntime.write[A](
                Expr.splice(fields),
                Expr.splice(out),
                Expr.splice(index),
                Expr.splice(value),
                Expr.splice(codecs[A])
              )
            }
        }
      }) { (first, second) =>
        Expr.quote {
          Expr.splice(first)
          Expr.splice(second)
        }
      }.getOrElse(Expr.quote(()))

    protected def sumCodec[T: Type](leaves: List[(String, ??)]): Codecs => Expr[JsonValueCodec[T]] =
      Environment.reportErrorAndAbort(s"${Type[T].prettyPrint} is a sealed hierarchy")
  }
}
