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
  private val JsonFormatType               = t"_root_.spray.json.JsonFormat"
  private def JsonFormat(T: Type)          = t"$JsonFormatType[$T]"
  private def implicitReader(T: Type)      = q"_root_.spray.json.jsonReader[$T]"
  private def implicitWriter(T: Type)      = q"_root_.spray.json.jsonWriter[$T]"
  private val IllegalArgumentExceptionType = t"IllegalArgumentException"

  def generateImplicit(taggedType: MetaModel.TaggedType) = {
    def readFunction(applyM: Term, arg: Term.Name) = q"""try $applyM(${implicitReader(taggedType.baseType)}.read($arg))
          catch { case e: $IllegalArgumentExceptionType => _root_.spray.json.deserializationError(e.getMessage, e) }
       """
    def writeFunction(arg: Term.Name) = q"""${implicitWriter(taggedType.baseType)}.write($arg)"""

    def formatGeneric(rep: TagTypeRep.GenericTrait, params: immutable.Seq[Type.Param]) = {
      val jsonArg = Term.Name("json")
      val objArg = Term.Name("obj")
      val t = taggedType.applied(rep.applied(params))
      val anon    = ctor"_root_.spray.json.JsonFormat[$t]"
      q"""new $anon {
            override def read($jsonArg: _root_.spray.json.JsValue) =
              ${readFunction(Term.ApplyType(taggedType.companionName, MetaUtils.reified(params)), jsonArg)}
            override def write($objArg: $t) = ${writeFunction(objArg)}
      }
       """
    }

    def format(rep: TagTypeRep.EmptyTrait) = {
      val jsonArg = Term.Name("json")
      val objArg = Term.Name("obj")
      val t = taggedType.applied(rep.name)

      q"""implicit object ${implicitName(rep)} extends _root_.spray.json.JsonFormat[$t] {
            override def read($jsonArg: _root_.spray.json.JsValue) = ${readFunction(taggedType.companionName, jsonArg)}
            override def write($objArg: $t) = ${writeFunction(objArg)}
      }
       """
    }

    def implicitName(rep: TagTypeRep)               = Term.Name(rep.termName.value + "JsonFormat")
    def implicitDefType(rep: TagTypeRep.GenericTrait, params: immutable.Seq[Type.Param]) =
      JsonFormat(taggedType.applied(rep.applied(params)))

    taggedType.tagType match {
      case rep @ TagTypeRep.EmptyTrait(_) => format(rep)
      case rep @ TagTypeRep.GenericTrait(_, params) =>
        q"""implicit def ${implicitName(rep)}[..${MetaUtils.invariant(params)}]: ${implicitDefType(rep, params)} =
              ${formatGeneric(rep, params)}"""
    }

  }
}
