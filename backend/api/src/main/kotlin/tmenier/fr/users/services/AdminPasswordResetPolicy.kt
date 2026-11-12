package tmenier.fr.users.services

object AdminPasswordResetPolicy {
    enum class Decision {
        CLEAR_MARKER,
        NOTHING,
        UNKNOWN_ACCOUNT,
        NOT_ADMIN,
        RESET,
        ALREADY_APPLIED,
    }

    fun decide(
        requestedEmail: String?,
        lastResetEmail: String?,
        accountFound: Boolean,
        accountIsAdmin: Boolean,
        passwordChangePending: Boolean,
    ): Decision {
        val requested = requestedEmail?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }

        return when {
            requested == null -> if (lastResetEmail != null) Decision.CLEAR_MARKER else Decision.NOTHING
            !accountFound -> Decision.UNKNOWN_ACCOUNT
            !accountIsAdmin -> Decision.NOT_ADMIN

            requested == lastResetEmail?.lowercase() && !passwordChangePending -> Decision.ALREADY_APPLIED
            else -> Decision.RESET
        }
    }
}
