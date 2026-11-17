package tmenier.fr.common.enums.backup

enum class RestoreIssueCode(
    val blocking: Boolean,
) {
    MISSING_KEY(blocking = true),
    SECRET_UNREADABLE(blocking = true),
    INVALID(blocking = true),
    ALREADY_EXISTS(blocking = false),
    DUPLICATE_IN_FILE(blocking = false),
    UNKNOWN_REFERENCE(blocking = false),
    PUSH_URL_REGENERATED(blocking = false),
}
