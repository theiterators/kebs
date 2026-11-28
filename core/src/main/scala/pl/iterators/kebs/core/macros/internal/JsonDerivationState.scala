package pl.iterators.kebs.core.macros.internal

import scala.collection.mutable

// what kebs' macros are doing, for the entry points their own implicit searches reach
private[kebs] object JsonDerivationState {

  // an instance an entry point was asked for; the type belongs to the compiler, not to one macro expansion
  final case class Request(typeClass: String, key: String, tpe: AnyRef)

  // the requests of a search, and whether an entry point yielded to an existing instance, which need not be of the type
  // class that was asked for
  final class Requests {
    private val requests           = mutable.ListBuffer.empty[Request]
    var yielded: Boolean           = false
    def +=(request: Request): Unit = requests += request
    def toList: List[Request]      = requests.toList
  }

  // a root derivation's searches share their lexical scope, so they get the same answers
  final private class Root {
    val answers: mutable.Map[String, Boolean] = mutable.Map.empty
    var crash: Option[Throwable]              = None
  }

  final private case class State(
      root: Option[Root] = None,
      asked: Set[String] = Set.empty,
      requests: Option[Requests] = None,
      insisting: Boolean = false,
      searchOwner: Option[AnyRef] = None
  )

  private val state = new ThreadLocal[State] { override def initialValue(): State = State() }

  private def during[A](next: State)(body: => A): A = {
    val before = state.get
    state.set(next)
    try body
    finally state.set(before)
  }

  def isSearching: Boolean   = state.get.searchOwner.isDefined
  def isDiscovering: Boolean = isSearching && state.get.requests.isDefined
  def isInsisting: Boolean   = state.get.insisting
  def isRecording: Boolean   = state.get.requests.isDefined

  def asRoot[A](body: => A): A = during(State())(body)

  // A search can make the compiler type another definition, whose result type is inferred. Entry points met there are not
  // part of the search.
  def entering[A](isInside: AnyRef => Boolean)(body: => A): A =
    if (state.get.searchOwner.exists(isInside)) body else asRoot(body)

  def searching[A](owner: AnyRef)(search: => A): A = during(state.get.copy(searchOwner = Some(owner)))(search)

  def inOneScope[A](discovery: => A): A = during(state.get.copy(root = Some(new Root)))(discovery)
  def insistingOn[A](search: => A): A   = during(state.get.copy(insisting = true))(search)

  def request(request: Request): Unit = state.get.requests.foreach(_ += request)
  def yieldToExisting(): Unit         = state.get.requests.foreach(_.yielded = true)

  def discovering[A](search: => A): (A, Requests) =
    if (state.get.root.isEmpty) inOneScope(discovering(search))
    else {
      val requests = new Requests
      val result   = during(state.get.copy(requests = Some(requests)))(search)
      state.get.root.flatMap(_.crash).foreach(crash => throw crash)
      (result, requests)
    }

  // an instance that needs itself would be looked for endlessly
  def exists(key: String)(search: => Boolean): Boolean = {
    val current = state.get
    if (current.asked.contains(key)) false
    else {
      def ask = during(current.copy(asked = current.asked + key))(discovering(search)._1)
      current.root.fold(ask)(_.answers.getOrElseUpdate(key, ask))
    }
  }

  // the compiler takes a crash in an entry point it tries for the lack of an instance, and would go on without a word
  def entryPoint[A](isAbort: Throwable => Boolean)(body: => A): A =
    try body
    catch {
      case crash: Throwable if !isAbort(crash) =>
        state.get.root.foreach(root => root.crash = root.crash.orElse(Some(crash)))
        throw crash
    }
}
