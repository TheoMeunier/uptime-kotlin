package tmenier.fr.users.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import tmenier.fr.users.services.AdminPasswordResetPolicy.Decision

class AdminPasswordResetPolicyTest {
    private fun decide(
        requested: String?,
        last: String? = null,
        found: Boolean = true,
        admin: Boolean = true,
        pending: Boolean = false,
    ) = AdminPasswordResetPolicy.decide(requested, last, found, admin, pending)

    @Test
    fun `no variable and no previous reset does nothing`() {
        assertEquals(Decision.NOTHING, decide(null))
        assertEquals(Decision.NOTHING, decide("   "))
    }

    @Test
    fun `removing the variable forgets the previous reset`() {
        assertEquals(Decision.CLEAR_MARKER, decide(null, last = "admin@example.com"))
    }

    @Test
    fun `a first reset applies`() {
        assertEquals(Decision.RESET, decide("admin@example.com"))
    }

    @Test
    fun `a variable left in place after the new password is chosen does not reset again`() {
        assertEquals(Decision.ALREADY_APPLIED, decide("Admin@Example.com ", last = "admin@example.com", pending = false))
    }

    @Test
    fun `a restart before the new password is chosen prints a fresh one`() {
        assertEquals(Decision.RESET, decide("admin@example.com", last = "admin@example.com", pending = true))
    }

    @Test
    fun `another e-mail than the last reset applies`() {
        assertEquals(Decision.RESET, decide("other@example.com", last = "admin@example.com"))
    }

    @Test
    fun `unknown accounts and viewers are refused`() {
        assertEquals(Decision.UNKNOWN_ACCOUNT, decide("ghost@example.com", found = false, admin = false))
        assertEquals(Decision.NOT_ADMIN, decide("viewer@example.com", admin = false))
    }
}
