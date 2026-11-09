package tmenier.fr.schedulers.templates

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PingOutputParserTest {
    @Test
    fun `reads rtt from iputils output and ignores the summary time`() {
        val output =
            """
            PING example.com (93.184.215.14) 56(84) bytes of data.
            64 bytes from 93.184.215.14: icmp_seq=1 ttl=56 time=12.3 ms

            --- example.com ping statistics ---
            1 packets transmitted, 1 received, 0% packet loss, time 0ms
            rtt min/avg/max/mdev = 12.300/12.300/12.300/0.000 ms
            """.trimIndent()

        assertEquals(12.3, PingOutputParser.rttMillis(output))
    }

    @Test
    fun `reads rtt from busybox output`() {
        val output = "64 bytes from 127.0.0.1: seq=0 ttl=64 time=0.045 ms"
        assertEquals(0.045, PingOutputParser.rttMillis(output))
    }

    @Test
    fun `reads rtt from macOS output`() {
        val output = "64 bytes from 1.1.1.1: icmp_seq=0 ttl=57 time=8.912 ms"
        assertEquals(8.912, PingOutputParser.rttMillis(output))
    }

    @Test
    fun `accepts sub-millisecond and comma decimal variants`() {
        assertEquals(1.0, PingOutputParser.rttMillis("Reply from 10.0.0.1: bytes=32 time<1ms TTL=64"))
        assertEquals(0.5, PingOutputParser.rttMillis("64 bytes from 10.0.0.1: icmp_seq=1 ttl=64 time=0,5 ms"))
    }

    @Test
    fun `falls back to the summary average when no per-packet line is printed`() {
        val macOs =
            """
            PING 10.82.28.1 (10.82.28.1): 56 data bytes

            --- 10.82.28.1 ping statistics ---
            1 packets transmitted, 1 packets received, 0.0% packet loss, 1 packets out of wait time
            round-trip min/avg/max/stddev = 7.013/7.013/7.013/0.000 ms
            """.trimIndent()
        assertEquals(7.013, PingOutputParser.rttMillis(macOs))
        assertEquals(12.3, PingOutputParser.rttMillis("rtt min/avg/max/mdev = 12.300/12.300/12.300/0.000 ms"))
    }

    @Test
    fun `returns null when no rtt is present`() {
        val output = "1 packets transmitted, 0 received, 100% packet loss, time 0ms"
        assertNull(PingOutputParser.rttMillis(output))
    }

    @Test
    fun `reads the resolved address from iputils, macOS and IPv6 headers`() {
        assertEquals(
            "93.184.215.14",
            PingOutputParser.resolvedAddress("PING home.example.com (93.184.215.14) 56(84) bytes of data."),
        )
        assertEquals("1.1.1.1", PingOutputParser.resolvedAddress("PING one.one.one.one (1.1.1.1): 56 data bytes"))
        assertEquals(
            "2a00:1450:4007:80c::200e",
            PingOutputParser.resolvedAddress("PING ipv6.google.com(par21s19-in-x0e.1e100.net (2a00:1450:4007:80c::200e)) 56 data bytes"),
        )
        assertNull(PingOutputParser.resolvedAddress("ping: home.example.com: Name or service not known"))
    }

    @Test
    fun `detects hostname resolution failures`() {
        assertTrue(PingOutputParser.isUnresolvedHost("ping: home.example.com: Name or service not known"))
        assertTrue(PingOutputParser.isUnresolvedHost("ping: home.example.com: Temporary failure in name resolution"))
        assertTrue(PingOutputParser.isUnresolvedHost("ping: cannot resolve home.example.com: Unknown host"))
        assertTrue(PingOutputParser.isUnresolvedHost("ping: bad address 'home.example.com'"))
        assertFalse(PingOutputParser.isUnresolvedHost("1 packets transmitted, 0 received, 100% packet loss, time 0ms"))
    }

    @Test
    fun `extracts the host from hostnames, IPs and legacy URLs`() {
        assertEquals("home.example.com", PingTarget.host("  home.example.com "))
        assertEquals("home.example.com", PingTarget.host("https://home.example.com:8443/path"))
        assertEquals("192.168.1.1", PingTarget.host("192.168.1.1"))
        assertEquals("192.168.1.1", PingTarget.host("http://192.168.1.1/"))
        assertEquals("2001:db8::1", PingTarget.host("2001:db8::1"))
        assertEquals("2001:db8::1", PingTarget.host("[2001:db8::1]"))
    }

    @Test
    fun `describes the target with its resolved address only when it differs`() {
        assertEquals("home.example.com (203.0.113.7)", PingTarget.describe("home.example.com", "203.0.113.7"))
        assertEquals("192.168.1.1", PingTarget.describe("192.168.1.1", "192.168.1.1"))
        assertEquals("home.example.com", PingTarget.describe("home.example.com", null))
    }
}
