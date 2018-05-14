package pl.iterators.kebs.tag.meta

import pl.iterators.kebs.tag.meta.MetaModel.Claitect

import scala.annotation.StaticAnnotation
import scala.meta._

class tagged extends StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    defn match {
      case Claitect(annotated) =>
        annotated.body.fold(defn) { statements =>
          val taggedTypes          = MetaModel.TaggedType.findAll(statements)
          val taggedTypeCompanions = taggedTypes.flatMap(_.maybeCompanion)
          val generated =
            taggedTypes.foldLeft(statements diff taggedTypeCompanions)((code, taggedType) => taggedType.generateCompanion +: code)
          annotated.replaced(generated)
        }
      case _ => abort(defn.pos, "@tagged must be used on object, trait or a class")
    }
  }
}
