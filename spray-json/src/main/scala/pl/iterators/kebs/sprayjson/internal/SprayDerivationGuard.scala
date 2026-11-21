package pl.iterators.kebs.sprayjson.internal

// types whose derivation is on the macro stack: on Scala 2 an implicit search started inside the macro reaches kebs'
// entry point again, and deriving the same type there would never end
private[sprayjson] object SprayDerivationGuard {
  private val inProgress = new ThreadLocal[Set[String]] {
    override def initialValue(): Set[String] = Set.empty
  }

  def isBeingDerived(key: String): Boolean = inProgress.get.contains(key)

  def deriving[A](key: String)(body: => A): A = {
    val before = inProgress.get
    inProgress.set(before + key)
    try body
    finally inProgress.set(before)
  }
}
