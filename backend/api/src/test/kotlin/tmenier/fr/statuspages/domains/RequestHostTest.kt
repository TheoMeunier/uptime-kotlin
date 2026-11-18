package tmenier.fr.statuspages.domains

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class RequestHostTest {
    @Test
    fun `normalizes case, port, trailing dot and proxy lists`() {
        assertEquals("status.example.com", RequestHost.normalize("Status.Example.COM"))
        assertEquals("status.example.com", RequestHost.normalize("status.example.com:8443"))
        assertEquals("status.example.com", RequestHost.normalize("status.example.com."))
        assertEquals("status.example.com", RequestHost.normalize(" status.example.com , proxy.internal"))
        assertEquals("[::1]", RequestHost.normalize("[::1]:8080"))
    }

    @Test
    fun `blank values give no host`() {
        assertNull(RequestHost.normalize(null))
        assertNull(RequestHost.normalize("  "))
    }

    @Test
    fun `the forwarded host wins over the host header`() {
        val headers = mapOf("X-Forwarded-Host" to "status.example.com", "Host" to "uptime-kotlin-api:8080")
        assertEquals("status.example.com", RequestHost.resolve(headers::get))
        assertEquals("uptime.example.com", RequestHost.resolve(mapOf("Host" to "uptime.example.com")::get))
    }
}
