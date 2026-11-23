package tmenier.fr.statuspages.domains

object RequestHost {
    const val FORWARDED_HOST = "X-Forwarded-Host"
    const val HOST = "Host"

    fun resolve(header: (String) -> String?): String? = normalize(header(FORWARDED_HOST)) ?: normalize(header(HOST))

    fun normalize(raw: String?): String? {
        val first = raw?.substringBefore(',')?.trim()?.lowercase()
        if (first.isNullOrEmpty()) return null

        val withoutPort =
            if (first.startsWith("[")) {
                first.substringBefore(']') + "]"
            } else {
                first.substringBefore(':')
            }

        return withoutPort.trimEnd('.').ifEmpty { null }
    }
}
