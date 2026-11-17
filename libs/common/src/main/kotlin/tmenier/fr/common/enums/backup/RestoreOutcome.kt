package tmenier.fr.common.enums.backup

enum class RestoreOutcome(
    val writes: Boolean,
) {
    CREATE(writes = true),
    RENAME(writes = true),
    SKIP(writes = false),
    ERROR(writes = false),
}
