package pl.iterators.kebs.playjson.internal

import hearth.MacroCommons
import hearth.std.StdExtensions
import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.macros.ValueClassLike
import pl.iterators.kebs.core.macros.internal.JsonDerivationMacrosImpl
import pl.iterators.kebs.playjson.KebsPlayJson
import play.api.libs.json._

import scala.annotation.nowarn
import scala.collection.Factory
import scala.collection.mutable

private[playjson] trait PlayJsonMacrosImpl extends JsonDerivationMacrosImpl { this: MacroCommons & StdExtensions =>

  private def methodsOf[A](tpe: Type[A], names: Set[String]): Seq[UntypedMethod] =
    tpe.unsortedMethods.filter(method => names(method.name)).map(_.asUntyped)

  private lazy val readsEntryPoints  = methodsOf(Type.of[KebsPlayJson], Set("kebsAutoReads", "kebsAutoReadsSealed"))
  private lazy val writesEntryPoints = methodsOf(Type.of[KebsPlayJson], Set("kebsAutoWrites", "kebsAutoWritesSealed"))

  private lazy val readsIgnoredForFields = readsEntryPoints ++
    methodsOf(Type.of[DefaultReads], Set("ArrayReads", "charMapReads", "mapReads")) ++
    methodsOf(Type.of[LowPriorityDefaultReads], Set("keyMapReads", "traversableReads"))

  private lazy val writesIgnoredForFields = writesEntryPoints ++
    methodsOf(Type.of[DefaultWrites], Set("arrayWrites", "genericMapWrites", "keyMapWrites", "mapWrites", "OptionWrites", "someWrites")) ++
    methodsOf(Type.of[LowPriorityWrites], Set("iterableWrites", "iterableWrites2", "traversableWrites"))

  // implicit entry points consult existing instances first; explicit derive* calls always derive.
  // stepAside: fail when an instance exists, for an entry point whose failure only skips it
  def deriveReadsRoot[T: Type](config: Expr[JsonConfig], consultExisting: Boolean, stepAside: Boolean): Expr[Reads[T]] = {
    loadExtensions()
    implicit val ReadsT: Type[Reads[T]] = Type.of[Reads[T]]
    (if (consultExisting) summonInstance[Reads[T]](readsEntryPoints) else None) match {
      case Some(found) =>
        if (stepAside) Environment.reportErrorAndAbort(s"${Type[T].prettyPrint} already has a Reads")
        found
      case None => new ReadsDerivation(config).root[T]
    }
  }

  def deriveWritesRoot[T: Type](config: Expr[JsonConfig], consultExisting: Boolean, stepAside: Boolean): Expr[Writes[T]] = {
    loadExtensions()
    implicit val WritesT: Type[Writes[T]] = Type.of[Writes[T]]
    (if (consultExisting) summonInstance[Writes[T]](writesEntryPoints) else None) match {
      case Some(found) =>
        if (stepAside) Environment.reportErrorAndAbort(s"${Type[T].prettyPrint} already has a Writes")
        found
      case None => new WritesDerivation(config).root[T]
    }
  }

  def deriveOWrites[T: Type](config: Expr[JsonConfig]): Expr[OWrites[T]] = {
    val writes = deriveWritesRoot[T](config, consultExisting = false, stepAside = false)
    Expr.quote(PlayDerivationRuntime.asOWrites(Expr.splice(writes)))
  }

  def deriveOFormat[T: Type](config: Expr[JsonConfig]): Expr[OFormat[T]] = {
    val reads  = deriveReadsRoot[T](config, consultExisting = false, stepAside = false)
    val writes = deriveOWrites[T](config)
    Expr.quote(OFormat[T](Expr.splice(reads), Expr.splice(writes)))
  }

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

  final private class ReadsDerivation(config: Expr[JsonConfig]) extends JsonDerivation[Reads] {
    protected def codecType[A: Type]: Type[Reads[A]]   = Type.of[Reads[A]]
    protected def ignoredForFields: Seq[UntypedMethod] = readsIgnoredForFields

    protected def optionCodec[A: Type](inner: Expr[Reads[A]]): Expr[Reads[Option[A]]] =
      Expr.quote(PlayDerivationRuntime.optionReads[A](Expr.splice(inner)))

    @nowarn("msg=never used")
    protected def collectionCodec[Item: Type, R: Type, C: Type](
        inner: Expr[Reads[Item]],
        iterate: Expr[C] => Expr[Iterable[Item]],
        factory: Expr[Factory[Item, R]],
        build: Expr[mutable.Builder[Item, R]] => Expr[C]
    ): Expr[Reads[C]] = Expr.quote {
      PlayDerivationRuntime.collectionReads[Item, R, C](Expr.splice(inner), Expr.splice(factory))((builder: mutable.Builder[Item, R]) =>
        Expr.splice(build(Expr.quote(builder)))
      )
    }

    @nowarn("msg=never used")
    protected def stringMapCodec[V: Type, P: Type, R: Type, C: Type](
        inner: Expr[Reads[V]],
        iterate: Expr[C] => Expr[Iterable[P]],
        factory: Expr[Factory[P, R]],
        key: Expr[P] => Expr[String],
        value: Expr[P] => Expr[V],
        pair: (Expr[String], Expr[V]) => Expr[P],
        build: Expr[mutable.Builder[P, R]] => Expr[C]
    ): Expr[Reads[C]] = Expr.quote {
      PlayDerivationRuntime.stringMapReads[V, P, R, C](Expr.splice(inner), Expr.splice(factory))(
        (k: String, v: V) => Expr.splice(pair(Expr.quote(k), Expr.quote(v))),
        (builder: mutable.Builder[P, R]) => Expr.splice(build(Expr.quote(builder)))
      )
    }

    protected def flatCodec[T: Type, A: Type](rep: Expr[ValueClassLike[T, A]], inner: Expr[Reads[A]]): Expr[Reads[T]] =
      Expr.quote(PlayDerivationRuntime.flatReads[T, A](Expr.splice(rep), Expr.splice(inner)))

    protected def singletonCodec[T: Type](value: Expr[T]): Expr[Reads[T]] =
      Expr.quote(PlayDerivationRuntime.singletonReads[T](Expr.splice(value)))

    protected def productCodec[T: Type](product: ProductShape[T]): Expr[Reads[T]] = {
      val (scalaNames, renamed) = names(product.fields)
      Expr.quote {
        PlayDerivationRuntime.productReads[T](Expr.splice(config), Expr.splice(scalaNames), Expr.splice(renamed))(
          (names: PlayDerivationRuntime.Fields, json: JsValue) => Expr.splice(readFields(product, Expr.quote(names), Expr.quote(json)))
        )
      }
    }

    private def readFields[T: Type](
        product: ProductShape[T],
        names: Expr[PlayDerivationRuntime.Fields],
        json: Expr[JsValue]
    ): Expr[JsResult[T]] = {
      // a helper rather than `::`: Scala 3 desugars it to a local val, which then owned the codecs created here
      val results = product.fields.foldRight(Expr.quote(List.empty[JsResult[Any]])) { (field, rest) =>
        Expr.quote(PlayDerivationRuntime.prepend(Expr.splice(fieldResult(product, field, names, json)), Expr.splice(rest)))
      }
      Expr.quote {
        PlayDerivationRuntime.combine[T](Expr.splice(results), (values: Array[Any]) => Expr.splice(construct(product, Expr.quote(values))))
      }
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
                    Expr.splice(codec[X]),
                    Expr.splice(orElse.asInstanceOf[Expr[Option[X]]])
                  )
                )
              case None =>
                Expr.quote(
                  PlayDerivationRuntime.optionField[X](Expr.splice(json), Expr.splice(names), Expr.splice(index), Expr.splice(codec[X]))
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
                    Expr.splice(codec[A]),
                    Expr.splice(orElse)
                  )
                )
              case None =>
                Expr.quote(
                  PlayDerivationRuntime.field[A](Expr.splice(json), Expr.splice(names), Expr.splice(index), Expr.splice(codec[A]))
                )
            }
        }
    }

    protected def sumCodec[T: Type](leaves: List[(String, ??)]): Expr[Reads[T]] = {
      val names = caseNames(leaves)
      Expr.quote {
        PlayDerivationRuntime.sumReads[T](Expr.splice(config), Expr.splice(names))((index: Int, json: JsValue) =>
          Expr.splice(readCase[T](leaves, Expr.quote(index), Expr.quote(json)))
        )
      }
    }

    private def readCase[T: Type](leaves: List[(String, ??)], index: Expr[Int], json: Expr[JsValue]): Expr[JsResult[T]] =
      leaves.zipWithIndex.foldRight(Expr.quote(PlayDerivationRuntime.unreachableCase[JsResult[T]](Expr.splice(index)))) {
        case (((_, leaf), i), otherwise) =>
          import leaf.Underlying as C
          val ii = Expr(i)
          Expr.quote {
            if (Expr.splice(index) == Expr.splice(ii)) Expr.splice(codec[C]).reads(Expr.splice(json)).asInstanceOf[JsResult[T]]
            else Expr.splice(otherwise)
          }
      }
  }

  final private class WritesDerivation(config: Expr[JsonConfig]) extends JsonDerivation[Writes] {
    protected def codecType[A: Type]: Type[Writes[A]]  = Type.of[Writes[A]]
    protected def ignoredForFields: Seq[UntypedMethod] = writesIgnoredForFields

    protected def optionCodec[A: Type](inner: Expr[Writes[A]]): Expr[Writes[Option[A]]] =
      Expr.quote(PlayDerivationRuntime.optionWrites[A](Expr.splice(inner)))

    @nowarn("msg=never used")
    protected def collectionCodec[Item: Type, R: Type, C: Type](
        inner: Expr[Writes[Item]],
        iterate: Expr[C] => Expr[Iterable[Item]],
        factory: Expr[Factory[Item, R]],
        build: Expr[mutable.Builder[Item, R]] => Expr[C]
    ): Expr[Writes[C]] = Expr.quote {
      PlayDerivationRuntime.collectionWrites[Item, C](
        Expr.splice(inner),
        (collection: C) => Expr.splice(iterate(Expr.quote(collection)))
      )
    }

    @nowarn("msg=never used")
    protected def stringMapCodec[V: Type, P: Type, R: Type, C: Type](
        inner: Expr[Writes[V]],
        iterate: Expr[C] => Expr[Iterable[P]],
        factory: Expr[Factory[P, R]],
        key: Expr[P] => Expr[String],
        value: Expr[P] => Expr[V],
        pair: (Expr[String], Expr[V]) => Expr[P],
        build: Expr[mutable.Builder[P, R]] => Expr[C]
    ): Expr[Writes[C]] = Expr.quote {
      PlayDerivationRuntime.stringMapWrites[V, P, C](
        Expr.splice(inner),
        (collection: C) => Expr.splice(iterate(Expr.quote(collection)))
      )(
        (p: P) => Expr.splice(key(Expr.quote(p))),
        (p: P) => Expr.splice(value(Expr.quote(p)))
      )
    }

    protected def flatCodec[T: Type, A: Type](rep: Expr[ValueClassLike[T, A]], inner: Expr[Writes[A]]): Expr[Writes[T]] =
      Expr.quote(PlayDerivationRuntime.flatWrites[T, A](Expr.splice(rep), Expr.splice(inner)))

    @nowarn("msg=never used")
    protected def singletonCodec[T: Type](value: Expr[T]): Expr[Writes[T]] =
      Expr.quote(PlayDerivationRuntime.singletonWrites[T])

    protected def productCodec[T: Type](product: ProductShape[T]): Expr[Writes[T]] = {
      val (scalaNames, renamed) = names(product.fields)
      Expr.quote {
        PlayDerivationRuntime.productWrites[T](Expr.splice(config), Expr.splice(scalaNames), Expr.splice(renamed))(
          (names: PlayDerivationRuntime.Fields, obj: T) => Expr.splice(writeFields(product, Expr.quote(names), Expr.quote(obj)))
        )
      }
    }

    private def writeFields[T: Type](
        product: ProductShape[T],
        names: Expr[PlayDerivationRuntime.Fields],
        obj: Expr[T]
    ): Expr[List[(String, JsValue)]] =
      product.fields.filterNot(_.transient).foldRight(Expr.quote(List.empty[(String, JsValue)])) { (field, rest) =>
        val fieldType: ?? = field.tpe
        import fieldType.Underlying as A
        val index = Expr(field.index)
        val value = product.value[A](obj, field)
        product.optionItem(field) match {
          case Some(item) =>
            import item.Underlying as X
            Expr.quote(
              PlayDerivationRuntime.optionEntry[X](
                Expr.splice(names),
                Expr.splice(index),
                Expr.splice(value.asInstanceOf[Expr[Option[X]]]),
                Expr.splice(codec[X]),
                Expr.splice(rest)
              )
            )
          case None =>
            Expr.quote(
              PlayDerivationRuntime
                .entry[A](Expr.splice(names), Expr.splice(index), Expr.splice(value), Expr.splice(codec[A]), Expr.splice(rest))
            )
        }
      }

    protected def sumCodec[T: Type](leaves: List[(String, ??)]): Expr[Writes[T]] = {
      val names = caseNames(leaves)
      Expr.quote {
        PlayDerivationRuntime.sumWrites[T](Expr.splice(config), Expr.splice(names))((obj: T, wrap: (Int, JsValue) => JsObject) =>
          Expr.splice(writeCase[T](leaves, Expr.quote(obj), Expr.quote(wrap)))
        )
      }
    }

    // C is the concrete leaf type in the expansion; only this macro's own source sees it abstract
    @nowarn("msg=is unchecked since it is eliminated by erasure")
    private def writeCase[T: Type](leaves: List[(String, ??)], obj: Expr[T], wrap: Expr[(Int, JsValue) => JsObject]): Expr[JsObject] =
      leaves.zipWithIndex.foldRight(Expr.quote(PlayDerivationRuntime.unreachableCase[JsObject](Expr.splice(obj)))) {
        case (((_, leaf), index), otherwise) =>
          import leaf.Underlying as C
          val i = Expr(index)
          Expr.quote {
            if (Expr.splice(obj).isInstanceOf[C])
              Expr.splice(wrap).apply(Expr.splice(i), Expr.splice(codec[C]).writes(Expr.splice(obj).asInstanceOf[C]))
            else Expr.splice(otherwise)
          }
      }
  }
}
