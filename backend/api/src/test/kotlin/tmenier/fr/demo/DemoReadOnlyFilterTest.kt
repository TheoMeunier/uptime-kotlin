package tmenier.fr.demo

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DemoReadOnlyFilterTest {
    @Test
    fun `reads are always allowed`() {
        assertTrue(DemoReadOnlyFilter.isAllowed("GET", "/api/probes"))
        assertTrue(DemoReadOnlyFilter.isAllowed("get", "/api/auth/sessions"))
        assertTrue(DemoReadOnlyFilter.isAllowed("OPTIONS", "/api/probes/new"))
    }

    @Test
    fun `signing in and out and check now stay available`() {
        assertTrue(DemoReadOnlyFilter.isAllowed("POST", "/api/auth/login"))
        assertTrue(DemoReadOnlyFilter.isAllowed("POST", "/api/auth/refresh"))
        assertTrue(DemoReadOnlyFilter.isAllowed("POST", "/api/auth/logout"))
        assertTrue(DemoReadOnlyFilter.isAllowed("POST", "/api/probes/0b7e8a4e-6a52-4d0b-9a3c-1f2e3d4c5b6a/check-now"))
    }

    @Test
    fun `every other write is refused`() {
        listOf(
            "POST" to "/api/probes/new",
            "POST" to "/api/probes/0b7e8a4e-6a52-4d0b-9a3c-1f2e3d4c5b6a/update",
            "POST" to "/api/probes/0b7e8a4e-6a52-4d0b-9a3c-1f2e3d4c5b6a/update-on-off",
            "POST" to "/api/probes/0b7e8a4e-6a52-4d0b-9a3c-1f2e3d4c5b6a/logs/purge",
            "POST" to "/api/notifications/testing",
            "POST" to "/api/notifications/new",
            "POST" to "/api/status-pages/new",
            "POST" to "/api/maintenances/new",
            "PUT" to "/api/settings/retention",
            "POST" to "/api/profile/update/password",
            "POST" to "/api/profile/onboarding/complete",
            "POST" to "/api/auth/sessions/revoke-all",
            "DELETE" to "/api/auth/sessions/0b7e8a4e-6a52-4d0b-9a3c-1f2e3d4c5b6a",
            "POST" to "/api/app/setup/first-user",
            "POST" to "/api/auth/login/../../probes/new",
            "POST" to "/./api/probes/new",
            "POST" to "/q/anything",
        ).forEach { (method, path) ->
            assertFalse(DemoReadOnlyFilter.isAllowed(method, path), "$method $path should be refused")
        }
    }
}
