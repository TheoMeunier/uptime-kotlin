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
}
