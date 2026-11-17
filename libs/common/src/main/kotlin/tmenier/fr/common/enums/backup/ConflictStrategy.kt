package tmenier.fr.common.enums.backup

import tmenier.fr.common.exceptions.common.BadRequestException

enum class ConflictStrategy {
    SKIP,
    RENAME,
    ;

    companion object {
        fun fromQuery(value: String): ConflictStrategy =
            entries.firstOrNull { it.name.equals(value.trim(), ignoreCase = true) }
                ?: throw BadRequestException("on_conflict must be one of ${entries.joinToString { it.name.lowercase() }}")
    }
}
