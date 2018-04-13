package pl.iterators.kebs.tag.meta

import scala.collection.immutable
import scala.meta.{Defn, Mod, Stat, Type}

private[meta] object MetaUtils {

  object CompanionWithApply {
    def unapply(obj: Defn.Object): Option[Defn.Object] =
      obj.templ.stats.flatMap(_.collectFirst {
        case Defn.Def(_, name, _, _, _, _) if name.value == "apply" => obj
      })
  }

  object IsPublic {
    def unapply(mods: immutable.Seq[Mod]): Boolean = !mods.exists {
      case Mod.Private(_) | Mod.Protected(_) => true
    }
  }

  def reified(typeParams: immutable.Seq[Type.Param]): immutable.Seq[Type.Name]    = typeParams.map(tp => Type.Name(tp.name.value))
  def applied(name: Type.Name, typeParams: immutable.Seq[Type.Param]): Type.Apply = Type.Apply(name, reified(typeParams))

  def findCompanion(stats: immutable.Seq[Stat], of: Type.Name): Option[Defn.Object] = {
    val ofName = of.value
    stats.collectFirst {
      case companion @ Defn.Object(_, name, _) if name.value == ofName => companion
    }
  }

  def invariant(params: immutable.Seq[Type.Param]) = params.map(_.copy(mods = List.empty))

}
