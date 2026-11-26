package pl.iterators.kebs.core.macros.internal

import scala.quoted.Quotes
import scala.util.Try

// The compiler reports a search as diverging when it meets a type that is already being searched for, and a macro's search
// inherits the ones it was expanded in: a derives clause or an inline given can search for the very type that is derived.
// The history is not part of the macro API.
private[kebs] object FreshQuotes {
  def apply(quotes: Quotes): Quotes = withoutHistory(quotes).getOrElse {
    quotes.reflect.report.warning(
      "kebs cannot start an implicit search of its own with this compiler: inside another macro, recursive types may be derived although they have an instance"
    )
    quotes
  }

  private def withoutHistory(quotes: Quotes): Option[Quotes] = Try {
    val loader                                               = quotes.getClass.getClassLoader
    def method(owner: AnyRef, name: String, parameters: Int) =
      owner.getClass.getMethods.find(m => m.getName == name && m.getParameterCount == parameters).get
    val context = method(quotes, "ctx", 0).invoke(quotes)
    val fresh   = method(context, "fresh", 0).invoke(context)
    val history = loader.loadClass("dotty.tools.dotc.typer.SearchRoot").getDeclaredConstructor().newInstance().asInstanceOf[AnyRef]
    method(fresh, "setSearchHistory", 1).invoke(fresh, history)
    val impl = loader.loadClass("scala.quoted.runtime.impl.QuotesImpl$").getField("MODULE$").get(null)
    method(impl, "apply", 1).invoke(impl, fresh).asInstanceOf[Quotes]
  }.toOption
}
