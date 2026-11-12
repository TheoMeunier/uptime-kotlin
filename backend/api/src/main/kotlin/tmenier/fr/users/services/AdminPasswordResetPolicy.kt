package tmenier.fr.users.services

/**
 * What RESET_ADMIN_PASSWORD does at startup. Pure, so that the "variable left in place" cases
 * are tested without a database.
 */
object AdminPasswordResetPolicy {
    enum class Decision {
        /** Variable absent: forget the last reset, so that setting it again later works. */
        CLEAR_MARKER,

        /** Variable absent and nothing to forget. */
        NOTHING,

        /** No account with this e-mail. */
        UNKNOWN_ACCOUNT,

        /** The account exists but is not an admin. */
        NOT_ADMIN,

        /** Generate a temporary password and print it. */
        RESET,

        /** Already reset and the new password has been chosen since: the variable was left behind. */
        ALREADY_APPLIED,
    }

    /**
     * @param requestedEmail the variable, null or blank when absent
     * @param lastResetEmail the account reset by the previous run of the variable, if any
     * @param accountFound whether [requestedEmail] matches an account
     * @param accountIsAdmin whether that account is an admin
     * @param passwordChangePending whether that account still carries a temporary password
     */
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
            // Still pending: printing a fresh password again is what an admin who lost the logs needs.
            requested == lastResetEmail?.lowercase() && !passwordChangePending -> Decision.ALREADY_APPLIED
            else -> Decision.RESET
        }
    }
}
