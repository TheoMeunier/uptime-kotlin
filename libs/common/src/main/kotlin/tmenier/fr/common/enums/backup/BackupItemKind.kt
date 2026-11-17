package tmenier.fr.common.enums.backup

enum class BackupItemKind(
    val keyField: String,
    private val copySuffix: String,
) {
    NOTIFICATION("name", " (%d)"),
    PROBE("name", " (%d)"),
    MAINTENANCE("title", " (%d)"),
    STATUS_PAGE("slug", "-%d"),
    ANNOUNCEMENT("title", " (%d)"),
    ;

    fun copyKey(
        key: String,
        number: Int,
    ): String = key + copySuffix.format(number)
}
