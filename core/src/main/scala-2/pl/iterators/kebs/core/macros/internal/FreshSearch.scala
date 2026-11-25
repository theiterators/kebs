package pl.iterators.kebs.core.macros.internal

import scala.reflect.macros.blackbox
import scala.util.Try

// The compiler reports a search as diverging when it meets a type that is already being searched for, and a macro's search
// inherits the ones it was expanded in. Its type of interest is being searched for by definition, so a recursive type
// would never be found. The list is not part of the macro API.
private[kebs] object FreshSearch {
  def apply[A](c: blackbox.Context)(search: => A): A =
    openImplicits(c) match {
      case Some((read, write)) =>
        val open = read()
        write(Nil)
        try search
        finally write(open)
      case None => search
    }

  def isAvailable(c: blackbox.Context): Boolean = openImplicits(c).isDefined

  private def openImplicits(c: blackbox.Context): Option[(() => AnyRef, AnyRef => Unit)] = Try {
    def method(owner: AnyRef, name: String, parameters: Int) =
      owner.getClass.getMethods.find(m => m.getName == name && m.getParameterCount == parameters).get
    val typer   = method(c, "callsiteTyper", 0).invoke(c)
    val context = method(typer, "context", 0).invoke(typer)
    val read    = method(context, "openImplicits", 0)
    val write   = method(context, "openImplicits_$eq", 1)
    (() => read.invoke(context), (open: AnyRef) => { write.invoke(context, open); () })
  }.toOption
}
