package tmenier.fr.auth.services

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tmenier.fr.common.enums.users.UserRole

class RoleAccessFilterTest {
    private val id = "0b7e8a4e-6a52-4d0b-9a3c-1f2e3d4c5b6a"

    @Test
    fun `an admin may do everything`() {
        listOf(
            "POST" to "/api/probes/new",
            "GET" to "/api/probes/$id/edit",
            "GET" to "/api/users",
            "POST" to "/api/users/new",
            "POST" to "/api/users/$id/remove",
        ).forEach { (method, path) ->
            assertTrue(RoleAccessFilter.isAllowed(UserRole.ADMIN, method, path), "$method $path")
        }
    }

    @Test
    fun `a viewer reads the monitoring data`() {
        listOf(
            "/api/probes",
            "/api/probes/$id",
            "/api/incidents",
            "/api/incidents/export",
            "/api/maintenances",
            "/api/status-pages",
            "/api/notifications",
            "/api/dashboard/stats",
            "/api/auth/sessions",
        ).forEach { path ->
            assertTrue(RoleAccessFilter.isAllowed(UserRole.VIEWER, "GET", path), path)
        }
    }

    @Test
    fun `a viewer manages their own account`() {
        listOf(
            "POST" to "/api/auth/logout",
            "POST" to "/api/auth/refresh",
            "POST" to "/api/auth/sessions/revoke-all",
            "DELETE" to "/api/auth/sessions/$id",
            "POST" to "/api/profile/update",
            "POST" to "/api/profile/update/password",
            "POST" to "/api/profile/onboarding/complete",
        ).forEach { (method, path) ->
            assertTrue(RoleAccessFilter.isAllowed(UserRole.VIEWER, method, path), "$method $path")
        }
    }

    @Test
    fun `a viewer changes nothing else`() {
        listOf(
            "POST" to "/api/probes/new",
            "POST" to "/api/probes/$id/update",
            "POST" to "/api/probes/$id/update-on-off",
            "POST" to "/api/probes/$id/check-now",
            "POST" to "/api/probes/$id/logs/purge",
            "POST" to "/api/incidents/$id/acknowledge",
            "POST" to "/api/notifications/testing",
            "POST" to "/api/status-pages/new",
            "POST" to "/api/maintenances/new",
            "PUT" to "/api/settings/retention",
            "POST" to "/api/users/new",
            "POST" to "/api/profile/update/../../probes/new",
        ).forEach { (method, path) ->
            assertFalse(RoleAccessFilter.isAllowed(UserRole.VIEWER, method, path), "$method $path")
        }
    }

    @Test
    fun `a viewer sees neither monitor secrets nor users`() {
        assertFalse(RoleAccessFilter.isAllowed(UserRole.VIEWER, "GET", "/api/probes/$id/edit"))
        assertFalse(RoleAccessFilter.isAllowed(UserRole.VIEWER, "GET", "/api/users"))
    }

    @Test
    fun `an identity without a role is refused`() {
        assertFalse(RoleAccessFilter.isAllowed(null, "GET", "/api/probes"))
    }

    @Test
    fun `an account with a temporary password may only replace it`() {
        listOf(
            "POST" to "/api/profile/update/password",
            "POST" to "/api/auth/logout",
            "POST" to "/api/auth/refresh",
            "GET" to "/api/app/status",
            "GET" to "/api/version",
            "GET" to "/api/status/main",
            "GET" to "/api/status/main/uptime",
        ).forEach { (method, path) ->
            assertTrue(RoleAccessFilter.isAllowedWhilePasswordChangeRequired(method, path), "$method $path")
        }

        listOf(
            "GET" to "/api/probes",
            "GET" to "/api/users",
            "GET" to "/api/auth/sessions",
            "POST" to "/api/profile/update",
            "POST" to "/api/users/new",
            "POST" to "/api/auth/sessions/revoke-all",
            "POST" to "/api/profile/update/password/../../../probes/new",
            "GET" to "/api/status/main/../../probes",
        ).forEach { (method, path) ->
            assertFalse(RoleAccessFilter.isAllowedWhilePasswordChangeRequired(method, path), "$method $path")
        }
    }
}
