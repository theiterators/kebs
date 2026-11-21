package pl.iterators.kebs.core.macros.internal

/** Shared (Scala 2.13 + Scala 3) listing of the cases of an enum-like type. */
private[kebs] trait EnumCasesMacrosImpl { this: hearth.MacroCommons =>

  /** The cases of `E` - `scala.Enumeration` values, Scala 3 enum cases, case objects of a sealed hierarchy (nested sealed hierarchies
    * flattened) - in declaration order, if there is at least one and every one is a singleton.
    */
  def caseObjectsOf[E: Type]: Option[Expr[List[E]]] =
    // hearth 0.4.5 does not see through an alias of an alias (`type T = Obj.Alias; type Alias = Value`) - dealias first
    singletonCasesOf(UntypedType.dealias(UntypedType.fromTyped[E]).asTyped[E]).filter(_.nonEmpty).map { cases =>
      cases.foldRight(Expr.quote(List.empty[E])) { (head, tail) =>
        Expr.quote(Expr.splice(head) :: Expr.splice(tail))
      }
    }

  private def singletonCasesOf[E](tpe: Type[E]): Option[List[Expr[E]]] = {
    implicit val E: Type[E] = tpe
    Type.directChildren[E].flatMap { children =>
      val perChild: List[Option[List[Expr[E]]]] = children.toList.map { case (_, child) =>
        import child.Underlying as C
        Expr.singletonOf[C] match {
          case Some(singleton)          => Some(List(singleton.upcast[E]))
          case None if Type.isSealed[C] => singletonCasesOf(Type[C]).map(_.map(_.upcast[E]))
          case None                     => None
        }
      }
      if (perChild.forall(_.isDefined)) Some(perChild.flatMap(_.toList.flatten)) else None
    }
  }
}
