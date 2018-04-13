package pl.iterators.kebs.tag.meta

import scala.annotation.StaticAnnotation
import scala.meta._
import MetaModel.{TagTypeCompanion, TagTypeRep}

import scala.collection.immutable

class spray extends StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    defn match {
      case Defn.Object(mods, name, body) =>
        body.stats.fold(defn) { statements =>
          val taggedTypes        = MetaModel.TaggedType.findAll(statements)
          val tagTypeCompanions  = MetaModel.findTagCompanions(taggedTypes.map(_.tagType), statements)
          val generatedImplicits = taggedTypes.map(spray.generateImplicit)

          val generatedCompanions = tagTypeCompanions.zip(generatedImplicits).map {
            case (TagTypeCompanion(t, None), implicitVal) => q"""object ${t.termName} extends _root_.spray.json.DefaultJsonProtocol { $implicitVal }"""
            case (TagTypeCompanion(t, Some(obj)), implicitVal) =>
              q"""..${obj.mods} object ${t.termName} extends _root_.spray.json.DefaultJsonProtocol { ..${implicitVal +: obj.templ.stats.toList.flatten} }"""
          }
          val existingCompanions = tagTypeCompanions.collect { case TagTypeCompanion(_, Some(obj)) => obj }
          q"..$mods object $name { ..${(statements diff existingCompanions) ++ generatedCompanions} }"
        }
      case _ => abort(defn.pos, "@spray must be used on object")
    }
  }
}

object spray {
  private val JsValueType                  = t"_root_.spray.json.JsValue"
  private val JsonReaderType               = t"_root_.spray.json.JsonReader"
  private def JsonReader(T: Type)          = t"$JsonReaderType[$T]"
  private def implicitReader(T: Type)      = q"_root_.spray.json.jsonReader[$T]"
  private val IllegalArgumentExceptionType = t"IllegalArgumentException"

  def generateImplicit(taggedType: MetaModel.TaggedType) = {
    def readFunction(applyM: Term, arg: Term.Name) = q"""try $applyM(${implicitReader(taggedType.baseType)}.read($arg))
          catch { case e: $IllegalArgumentExceptionType => _root_.spray.json.deserializationError(e.getMessage, e) }
       """

    def reader(rep: TagTypeRep.GenericTrait, params: immutable.Seq[Type.Param]) = {
      val jsonArg = Term.Name("json")
      val anon    = ctor"_root_.spray.json.JsonReader[${taggedType.applied(rep.applied(params))}]"
      q"""new $anon {
            override def read(json: _root_.spray.json.JsValue) = 
              ${readFunction(Term.ApplyType(taggedType.companionName, MetaUtils.reified(params)), jsonArg)}
      }
       """
    }

    def implicitName(rep: TagTypeRep)               = Term.Name(rep.termName.value + "JsonReader")
    def implicitValType(rep: TagTypeRep.EmptyTrait) = JsonReader(taggedType.applied(rep.name))
    def implicitDefType(rep: TagTypeRep.GenericTrait, params: immutable.Seq[Type.Param]) =
      JsonReader(taggedType.applied(rep.applied(params)))
    val jsonArg = Term.Name("json")

    taggedType.tagType match {
      case rep @ TagTypeRep.EmptyTrait(_) =>
        q"""implicit val ${Pat.Var.Term(implicitName(rep))}: ${implicitValType(rep)} = ($jsonArg: $JsValueType) =>
              ${readFunction(taggedType.companionName, jsonArg)}"""
      case rep @ TagTypeRep.GenericTrait(_, params) =>
        q"""implicit def ${implicitName(rep)}[..${MetaUtils.invariant(params)}]: ${implicitDefType(rep, params)} =
              ${reader(rep, params)}"""
    }

  }
}
