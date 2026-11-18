package tmenier.fr.statuspages.domains

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CustomDomainScopeFilterTest {
    @Test
    fun `public status endpoints stay readable on a custom domain`() {
        assertTrue(CustomDomainScopeFilter.isAllowed("GET", "/api/status/acme"))
        assertTrue(CustomDomainScopeFilter.isAllowed("GET", "/api/status/acme/uptime"))
        assertTrue(CustomDomainScopeFilter.isAllowed("GET", "/api/status/acme/feed"))
        assertTrue(CustomDomainScopeFilter.isAllowed("GET", "/api/status-domains/resolve"))
        assertTrue(CustomDomainScopeFilter.isAllowed("OPTIONS", "/api/status/acme"))
    }

    @Test
    fun `the rest of the API is not served on a custom domain`() {
        listOf(
            "POST" to "/api/auth/login",
            "POST" to "/api/auth/refresh",
            "GET" to "/api/probes",
            "GET" to "/api/status-pages",
            "GET" to "/api/probes/status",
            "POST" to "/api/status/acme",
            "GET" to "/api/status/../probes",
            "GET" to "/api/status/acme/../../probes",
        ).forEach { (method, path) -> assertFalse(CustomDomainScopeFilter.isAllowed(method, path), "$method $path") }
    }
}
