package tmenier.fr.databases.repositories

object ProbeNameOrder {
    val NAMES: Comparator<String> = String.CASE_INSENSITIVE_ORDER.then(naturalOrder())

    fun <T> by(selector: (T) -> String): Comparator<T> = compareBy(NAMES, selector)
}
