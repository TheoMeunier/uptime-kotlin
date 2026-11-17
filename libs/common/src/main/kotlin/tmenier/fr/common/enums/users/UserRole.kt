package tmenier.fr.common.enums.users

enum class UserRole {
    ADMIN,
    VIEWER,
    ;

    val securityRole: String get() = name.lowercase()

    companion object {
        const val ADMIN_ROLE = "admin"
        const val VIEWER_ROLE = "viewer"
        const val PASSWORD_CHANGE_REQUIRED_ROLE = "password-change-required"
    }
}
