package tmenier.fr.common.enums.backup

import tmenier.fr.common.exceptions.common.BadRequestException

enum class SecretsMode {
    ENCRYPTED,
    PLAIN,
    ;

    companion object {
        fun fromQuery(value: String): SecretsMode =
            entries.firstOrNull { it.name.equals(value.trim(), ignoreCase = true) }
                ?: throw BadRequestException("secrets must be one of ${entries.joinToString { it.name.lowercase() }}")
    }
}
