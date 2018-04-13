package pl.iterators.kebs.tag.meta

import scala.annotation.StaticAnnotation
import scala.meta._

class tagged extends StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    defn match {
      case Defn.Object(mods, name, body) =>
        body.stats.fold(defn) { statements =>
          val tagTypes             = MetaModel.findTagTypes(statements)
          val taggedTypes          = MetaModel.TaggedType.findAll(statements, tagTypes)
          val taggedTypeCompanions = taggedTypes.flatMap(_.maybeCompanion)
          val generated =
            taggedTypes.foldLeft(statements diff taggedTypeCompanions)((code, taggedType) => taggedType.generateCompanion +: code)
          q"..$mods object $name { ..$generated }"
        }
      case _ => abort(defn.pos, "@tagged must be used on object")
    }
  }
}


