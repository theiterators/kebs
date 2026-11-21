package pl.iterators.kebs.sprayjson.internal

import hearth.MacroCommons
import hearth.std.StdExtensions
import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.macros.ValueClassLike
import pl.iterators.kebs.core.macros.internal.JsonDerivationMacrosImpl
import pl.iterators.kebs.sprayjson.KebsSprayJson
import spray.json.{CollectionFormats, JsValue, JsonFormat, RootJsonFormat, StandardFormats}

import scala.annotation.nowarn
import scala.collection.Factory
import scala.collection.mutable

private[sprayjson] trait SprayFormatMacrosImpl extends JsonDerivationMacrosImpl { this: MacroCommons & StdExtensions =>

  private def protocolMethods(names: Set[String]): Seq[UntypedMethod] =
    Type.of[KebsSprayJson].unsortedMethods.filter(method => names(method.name)).map(_.asUntyped)

  private lazy val derivationEntryPoints = protocolMethods(Set("kebsAutoJsonFormat", "kebsAutoJsonFormatSealed"))
  // kebs' own generic formats do not count as an existing root instance: a root request derives an object (kebs 2)
  private lazy val ignoredAtRoot =
    derivationEntryPoints ++ protocolMethods(Set("jsonFlatFormat", "jsonConversionFormat2"))
  private lazy val sprayIgnoredForFields = derivationEntryPoints ++
    (Type.of[CollectionFormats].unsortedMethods ++ Type.of[StandardFormats].unsortedMethods.filter(_.name == "optionFormat"))
      .filter(_.isImplicit)
      .map(_.asUntyped)

  // implicit entry points consult existing formats first; an explicit jsonFormatN/jsonFormatSealed call always derives.
  // stepAside: fail when a format exists, for an entry point whose failure only skips it
  def deriveRootFormat[T: Type](config: Expr[JsonConfig], consultExisting: Boolean, stepAside: Boolean): Expr[RootJsonFormat[T]] = {
    loadExtensions()
    implicit val JsonFormatT: Type[JsonFormat[T]] = Type.of[JsonFormat[T]]
    val format                                    = (if (consultExisting) summonInstance[JsonFormat[T]](ignoredAtRoot) else None) match {
      case Some(found) =>
        if (stepAside) Environment.reportErrorAndAbort(s"${Type[T].prettyPrint} already has a JsonFormat")
        found
      case None => new SprayDerivation(config).root[T]
    }
    Expr.quote(SprayDerivationRuntime.asRoot(Expr.splice(format)))
  }

  final private class SprayDerivation(config: Expr[JsonConfig]) extends JsonDerivation[JsonFormat] {
    protected def codecType[A: Type]: Type[JsonFormat[A]] = Type.of[JsonFormat[A]]
    protected def ignoredForFields: Seq[UntypedMethod]    = sprayIgnoredForFields

    protected def optionCodec[A: Type](inner: Expr[JsonFormat[A]]): Expr[JsonFormat[Option[A]]] =
      Expr.quote(SprayDerivationRuntime.optionFormat[A](Expr.splice(inner)))

    protected def collectionCodec[Item: Type, R: Type, C: Type](
        inner: Expr[JsonFormat[Item]],
        iterate: Expr[C] => Expr[Iterable[Item]],
        factory: Expr[Factory[Item, R]],
        build: Expr[mutable.Builder[Item, R]] => Expr[C]
    ): Expr[JsonFormat[C]] = Expr.quote {
      SprayDerivationRuntime.collectionFormat[Item, R, C](
        Expr.splice(inner),
        (collection: C) => Expr.splice(iterate(Expr.quote(collection))),
        Expr.splice(factory)
      )((builder: mutable.Builder[Item, R]) => Expr.splice(build(Expr.quote(builder))))
    }

    protected def stringMapCodec[V: Type, P: Type, R: Type, C: Type](
        inner: Expr[JsonFormat[V]],
        iterate: Expr[C] => Expr[Iterable[P]],
        factory: Expr[Factory[P, R]],
        key: Expr[P] => Expr[String],
        value: Expr[P] => Expr[V],
        pair: (Expr[String], Expr[V]) => Expr[P],
        build: Expr[mutable.Builder[P, R]] => Expr[C]
    ): Expr[JsonFormat[C]] = Expr.quote {
      SprayDerivationRuntime.stringMapFormat[V, P, R, C](
        Expr.splice(inner),
        (collection: C) => Expr.splice(iterate(Expr.quote(collection))),
        Expr.splice(factory)
      )(
        (p: P) => Expr.splice(key(Expr.quote(p))),
        (p: P) => Expr.splice(value(Expr.quote(p))),
        (k: String, v: V) => Expr.splice(pair(Expr.quote(k), Expr.quote(v))),
        (builder: mutable.Builder[P, R]) => Expr.splice(build(Expr.quote(builder)))
      )
    }

    protected def flatCodec[T: Type, A: Type](rep: Expr[ValueClassLike[T, A]], inner: Expr[JsonFormat[A]]): Expr[JsonFormat[T]] =
      Expr.quote(SprayDerivationRuntime.flatFormat[T, A](Expr.splice(rep), Expr.splice(inner)))

    protected def singletonCodec[T: Type](value: Expr[T]): Expr[JsonFormat[T]] =
      Expr.quote(SprayDerivationRuntime.singleton[T](Expr.splice(value)))

    protected def productCodec[T: Type](product: ProductShape[T]): Expr[JsonFormat[T]] = {
      implicit val StringType: Type[String]                   = Type.of[String]
      implicit val OptionStringType: Type[Option[String]]     = Type.of[Option[String]]
      implicit val ListStringType: Type[List[String]]         = Type.of[List[String]]
      implicit val ListOptionType: Type[List[Option[String]]] = Type.of[List[Option[String]]]
      val scalaNames                                          = Expr(product.fields.map(_.name))
      val renamed                                             = Expr(product.fields.map(_.renamed))
      Expr.quote {
        SprayDerivationRuntime.product[T](Expr.splice(config), Expr.splice(scalaNames), Expr.splice(renamed))(
          (names: SprayDerivationRuntime.Fields, obj: T) => Expr.splice(writeFields(product, Expr.quote(names), Expr.quote(obj))),
          (names: SprayDerivationRuntime.Fields, json: JsValue) => Expr.splice(readFields(product, Expr.quote(names), Expr.quote(json)))
        )
      }
    }

    private def writeFields[T: Type](
        product: ProductShape[T],
        names: Expr[SprayDerivationRuntime.Fields],
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
              SprayDerivationRuntime.optionEntry[X](
                Expr.splice(names),
                Expr.splice(index),
                Expr.splice(value.asInstanceOf[Expr[Option[X]]]),
                Expr.splice(codec[X]),
                Expr.splice(rest)
              )
            )
          case None =>
            Expr.quote(
              SprayDerivationRuntime
                .entry[A](Expr.splice(names), Expr.splice(index), Expr.splice(value), Expr.splice(codec[A]), Expr.splice(rest))
            )
        }
      }

    private def readFields[T: Type](product: ProductShape[T], names: Expr[SprayDerivationRuntime.Fields], json: Expr[JsValue]): Expr[T] =
      product.construct(product.fields.map(field => argument(product, field, names, json)))

    private def argument[T: Type](
        product: ProductShape[T],
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
                      Expr.splice(codec[X]),
                      Expr.splice(orElse.asInstanceOf[Expr[Option[X]]])
                    )
                  )
                case None =>
                  Expr.quote(
                    SprayDerivationRuntime.readOption[X](Expr.splice(json), Expr.splice(names), Expr.splice(index), Expr.splice(codec[X]))
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
                      Expr.splice(codec[A]),
                      Expr.splice(orElse)
                    )
                  )
                case None =>
                  Expr.quote(
                    SprayDerivationRuntime.read[A](Expr.splice(json), Expr.splice(names), Expr.splice(index), Expr.splice(codec[A]))
                  )
              }
          }
      value.as_??
    }

    protected def sumCodec[T: Type](leaves: List[(String, ??)]): Expr[JsonFormat[T]] = {
      implicit val StringType: Type[String]           = Type.of[String]
      implicit val ListStringType: Type[List[String]] = Type.of[List[String]]
      val caseNames                                   = Expr(leaves.map(_._1))
      Expr.quote {
        SprayDerivationRuntime.sum[T](Expr.splice(config), Expr.splice(caseNames))(
          (obj: T, wrap: (Int, JsValue) => JsValue) => Expr.splice(writeCase[T](leaves, Expr.quote(obj), Expr.quote(wrap))),
          (index: Int, json: JsValue) => Expr.splice(readCase[T](leaves, Expr.quote(index), Expr.quote(json)))
        )
      }
    }

    // C is the concrete leaf type in the expansion; only this macro's own source sees it abstract
    @nowarn("msg=is unchecked since it is eliminated by erasure")
    private def writeCase[T: Type](leaves: List[(String, ??)], obj: Expr[T], wrap: Expr[(Int, JsValue) => JsValue]): Expr[JsValue] =
      leaves.zipWithIndex.foldRight(Expr.quote(SprayDerivationRuntime.unreachableCase[JsValue](Expr.splice(obj)))) {
        case (((_, leaf), index), otherwise) =>
          import leaf.Underlying as C
          val i = Expr(index)
          Expr.quote {
            if (Expr.splice(obj).isInstanceOf[C])
              Expr.splice(wrap).apply(Expr.splice(i), Expr.splice(codec[C]).write(Expr.splice(obj).asInstanceOf[C]))
            else Expr.splice(otherwise)
          }
      }

    private def readCase[T: Type](leaves: List[(String, ??)], index: Expr[Int], json: Expr[JsValue]): Expr[T] =
      leaves.zipWithIndex.foldRight(Expr.quote(SprayDerivationRuntime.unreachableCase[T](Expr.splice(index)))) {
        case (((_, leaf), i), otherwise) =>
          import leaf.Underlying as C
          val ii = Expr(i)
          Expr.quote {
            if (Expr.splice(index) == Expr.splice(ii)) Expr.splice(codec[C]).read(Expr.splice(json)).asInstanceOf[T]
            else Expr.splice(otherwise)
          }
      }
  }
}
