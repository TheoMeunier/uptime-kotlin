package tmenier.fr.statuspages.domains

/**
 * Host name a visitor typed, as seen behind the reverse proxies.
 *
 * `X-Forwarded-Host` wins over `Host` so that a proxy rewriting `Host` still works. Neither header is trusted for
 * access control: the host only picks which *public* status page to show.
 */
object RequestHost {
    const val FORWARDED_HOST = "X-Forwarded-Host"
    const val HOST = "Host"

    fun resolve(header: (String) -> String?): String? = normalize(header(FORWARDED_HOST)) ?: normalize(header(HOST))

    /** Lower case, first value only, without port nor trailing dot. `null` when blank. */
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
