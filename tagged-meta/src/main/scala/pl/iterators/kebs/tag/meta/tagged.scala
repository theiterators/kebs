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
          val tagTypes             = taggedTypes.map(_.tagType)
          val tagTypeCompanions    = tagTypes.flatMap(_.maybeCompanion)
          val allCompanions        = taggedTypeCompanions ++ tagTypeCompanions

          val generated =
            taggedTypes.foldLeft(statements diff allCompanions)((code, taggedType) =>
              taggedType.generateCompanion +: taggedType.generateTagCompanion +: code)
          annotated.replaced(generated)
        }
      case _ => abort(defn.pos, "@tagged must be used on object, trait or a class")
    }
  }
}
