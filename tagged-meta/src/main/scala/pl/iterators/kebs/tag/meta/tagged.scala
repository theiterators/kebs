package pl.iterators.kebs.tag.meta

import scala.language.existentials
import scala.language.experimental.macros
import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.reflect.macros.whitebox

@compileTimeOnly("""
                   |Please enable the macro paradise. If you are using Scala 2.13, this can be done by enabling
                   |-Ymacro-annotations compiler flag. If you are using Scala 2.12 or earlier, you will need to add a compiler plugin
                   |org.scalamacros.paradise. Using sbt: addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
                 """.stripMargin('|'))
class tagged extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro macroImpl.impl
}

final class macroImpl(val c: whitebox.Context) {

  import c.universe._

  def impl(annottees: Expr[Any]*): Expr[Any] = {
    val tree = annottees.map(_.tree).toList

    // There will be at most 2 elements in the list. If a class or trait is annotated, the companion object
    // will also be included. Although if companion object is annotated, only the companion object will be included
    val result: Tree = tree match {
      case ClassDef(mods, className, typeParams, template) :: tail =>
        val newBody     = generate(template.body)
        val newTemplate = Template(template.parents, template.self, newBody)
        val newClass    = ClassDef(mods, className, typeParams, newTemplate)

        // We need to return all annottees that we got as an input
        tail match {
          case companion :: _ => q"$newClass; $companion"
          case Nil            => q"$newClass"
        }

      case ModuleDef(mods, moduleName, template) :: Nil =>
        val newBody     = generate(template.body)
        val newTemplate = Template(template.parents, template.self, newBody)
        val newModule   = ModuleDef(mods, moduleName, newTemplate)
        newModule

      case _ =>
        c.abort(tree.head.pos, "@tagged must be used on object, trait or class")
    }

    c.Expr[Any](result)
  }

  def generate(trees: List[Tree]): List[Tree] = {
    val taggedTypes          = findAllTags(trees)
    val taggedTypeCompanions = taggedTypes.flatMap(_.maybeCompanion)
    val tagTypes             = taggedTypes.map(_.tagType).distinct
    val tagTypeCompanions    = tagTypes.flatMap(_.maybeCompanion)

    val allCompanions = taggedTypeCompanions ++ tagTypeCompanions
    val generatedTagCompanions =
      taggedTypes.groupBy(_.tagType).iterator.map { case (tag, taggedTypes) => tag.generateCompanion(taggedTypes) }.toList
    val generatedTaggedTypeCompanions = taggedTypes.map(taggedType => taggedType.generateCompanion)

    generatedTagCompanions ++ (trees diff allCompanions) ++ generatedTaggedTypeCompanions
  }

  case class TaggedType(name: TypeName,
                        typeParams: List[TypeDef],
                        baseTypeName: TypeName,
                        baseParams: List[TypeDef],
                        tagType: TagTypeRep,
                        maybeCompanion: Option[ModuleDef]) {

    private val selfType = tq"$name[..${typeArguments(typeParams)}]"

    def generateCompanion: Tree = {
      maybeCompanion match {
        case Some(companion @ ModuleDef(_, _, template)) if containsApply(template.body) =>
          companion
        case Some(ModuleDef(mods, companionName, template)) =>
          val generatedMethods = List(generateFromMethod, generateApplyMethod)
          ModuleDef(mods, companionName, Template(template.parents, template.self, template.body ++ generatedMethods))
        case None =>
          q"object ${name.toTermName} { ..${List(generateFromMethod, generateApplyMethod)} }"
      }
    }

    def generateFromMethod: Tree = {
      val argName = TermName("arg")
      // Determining whether the tag has type parameters. We don't allow both base type and tag type to be
      // polymorphic, that's why we can use all the type parameters for the tag type if the base type does not have
      // type parameters
      val tagParams =
        if (typeParams.nonEmpty && baseParams.isEmpty)
          typeArguments(typeParams)
        else List.empty
      val body =
        if (hasValidations)
          q"validate($argName).right.map(arg1 => $argName.taggedWith[${tagType.tagName}[..$tagParams]])"
        else
          q"$argName.taggedWith[${tagType.tagName}[..$tagParams]]"

      q"def from[..$typeParams](arg: $baseTypeName[..$baseParams]) = $body"
    }

    def generateApplyMethod: Tree = {
      val arg      = TermName("arg")
      val fromCall = if (typeParams.nonEmpty) q"from[..${typeArguments(typeParams)}]($arg)" else q"from($arg)"
      val body =
        if (hasValidations)
          q"$fromCall.fold(l => throw new IllegalArgumentException(l.toString), identity)"
        else
          fromCall

      q"def apply[..$typeParams](arg: $baseTypeName[..$baseParams]) = $body"
    }

    def generateCaseClass1RepImplicit: Tree = {
      val caseClass1RepInstanceTree =
        q"new _root_.pl.iterators.kebs.macros.CaseClass1Rep[$selfType, $baseTypeName[..$baseParams]](${name.toTermName}.apply(_), identity)"
      val implicitName = TermName(name.decodedName.toString + "CaseClass1Rep")

      if (typeParams.isEmpty)
        q"implicit val $implicitName = $caseClass1RepInstanceTree"
      else
        q"implicit def $implicitName[..$typeParams] = $caseClass1RepInstanceTree"
    }

    private def containsApply(trees: List[Tree]): Boolean = {
      trees.exists {
        case q"def apply[..$_](..$_): $_ = $_" => true
        case _                                 => false
      }
    }

    // validation must be a method which is:
    //  - public
    //  - named with {ValidationMethodName}
    //  - has no type parameters
    //  - takes a single argument
    //  - it needs to return an Either instance but this is not enforced here (will result in a compilation error later)
    private def hasValidations: Boolean =
      maybeCompanion.toList
        .flatMap(_.impl.body)
        .exists(_.exists {
          case DefDef((mods, TermName("validate"), Nil, List(_ :: Nil), _, _))
              if !mods.hasFlag(Flag.PRIVATE) && !mods.hasFlag(Flag.PROTECTED) =>
            true
          case _ => false
        })
  }

  case class TagTypeRep(tagName: TypeName, maybeCompanion: Option[ModuleDef]) {

    def generateCompanion(taggedTypes: List[TaggedType]): Tree = {
      val implicits = taggedTypes.map(_.generateCaseClass1RepImplicit)
      maybeCompanion match {
        case Some(ModuleDef(mods, companionName, template)) =>
          ModuleDef(mods, companionName, Template(template.parents, template.self, template.body ++ implicits))
        case None =>
          q"object ${tagName.toTermName} { ..$implicits }"
      }
    }

  }

  def findAllTags(trees: List[Tree]): List[TaggedType] = {
    val tagTypeReps = trees.collect {
      case ClassDef(modifiers, tagName, _, template) if modifiers.hasFlag(Flag.TRAIT) && template.body.isEmpty =>
        TagTypeRep(tagName, findCompanion(trees, tagName))
    }

    if (tagTypeReps.isEmpty) List.empty
    else
      trees.flatMap {
        case q"type ${taggedTypeName: TypeName}[..${params: List[TypeDef]}] = @@[${baseName: TypeName}[..${baseParams: List[
              TypeDef @unchecked]}], ${tagName: TypeName}[..$_]]" =>
          tagTypeReps
            .find(_.tagName == tagName)
            .map(tagType => TaggedType(taggedTypeName, params, baseName, baseParams, tagType, findCompanion(trees, taggedTypeName)))
        case _ =>
          None
      }
  }

  def findCompanion(trees: List[Tree], of: TypeName): Option[ModuleDef] = {
    trees.collectFirst {
      case t @ ModuleDef(_, name, _) if name.decodedName.toString == of.decodedName.toString => t
    }
  }

  def typeArguments(typeParams: List[TypeDef]): List[TypeName] =
    typeParams.map(_.name)

}
