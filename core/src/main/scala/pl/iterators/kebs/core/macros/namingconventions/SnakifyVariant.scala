package pl.iterators.kebs.core.macros.namingconventions

object SnakifyVariant {
  private val PASS_1 = """([A-Z\d]+)([A-Z][a-z])""".r
  private val PASS_2 = """([a-z\d])([A-Z])""".r

  private def isCamelCased(word: String) = word.exists(ch => ch == '-' || ch.isUpper)

  def snakify(word: String): String = {
    if (!isCamelCased(word)) word
    else {
      val afterPass1 = PASS_1.replaceAllIn(word, "$1_$2")
      val afterPass2 = PASS_2.replaceAllIn(afterPass1, "$1_$2")

      afterPass2.replace('-', '_').toLowerCase
    }
  }
}

object CapitalizeVariant {
  def capitalize(word: String): String = word.capitalize
}
