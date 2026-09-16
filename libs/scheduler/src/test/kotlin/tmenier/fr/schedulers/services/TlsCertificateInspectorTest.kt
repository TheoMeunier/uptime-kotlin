package tmenier.fr.schedulers.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.dtos.SslCertificateInfo
import tmenier.fr.common.enums.monitors.HttpCodeEnum
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.databases.dtos.ProbeDTO
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

class TlsCertificateInspectorTest {
    private val inspector = TlsCertificateInspector(SslCertificateService())
    private val now: Instant = Instant.parse("2026-09-16T12:00:00Z")

    @Test
    fun `a step without TLS is never inspected`() {
        val content = content()

        assertFalse(inspector.isInspectionDue(probe(), content, step("http://example.test"), now))
    }

    @Test
    fun `a probe that ignores certificate errors is never inspected`() {
        val content = content(ignoreCertificateErrors = true, notificationCertified = true)

        assertFalse(inspector.isInspectionDue(probe(), content, step(), now))
    }

    @Test
    fun `a probe alerting on expiry is inspected on every check`() {
        val content = content(notificationCertified = true)
        val readAMinuteAgo = probe(tlsCheckedAt = now.minus(Duration.ofMinutes(1)))

        assertTrue(inspector.isInspectionDue(readAMinuteAgo, content, step(), now))
    }

    @Test
    fun `a certificate never read is inspected`() {
        assertTrue(inspector.isInspectionDue(probe(tlsCheckedAt = null), content(), step(), now))
    }

    @Test
    fun `a certificate read within the day is left alone`() {
        val readThisMorning = probe(tlsCheckedAt = now.minus(Duration.ofHours(23)))

        assertFalse(inspector.isInspectionDue(readThisMorning, content(), step(), now))
    }

    @Test
    fun `a certificate read more than a day ago is inspected again`() {
        val readYesterday = probe(tlsCheckedAt = now.minus(Duration.ofHours(25)))

        assertTrue(inspector.isInspectionDue(readYesterday, content(), step(), now))
    }

    @Test
    fun `an expiring certificate fails the check only when the probe alerts on expiry`() {
        val expiringSoon = certificate(daysUntilExpiration = 3, isExpiringSoon = true)

        val informative = inspector.interpret(content(), expiringSoon, now)
        val alerting = inspector.interpret(content(notificationCertified = true), expiringSoon, now)

        assertNull(informative.failure)
        assertEquals(expiringSoon.expirationDate, informative.expiresAt)
        assertNotNull(alerting.failure)
        assertTrue(alerting.failure!!.contains("3 days"))
    }

    @Test
    fun `a handshake that gave nothing still counts as a read`() {
        val inspection = inspector.interpret(content(notificationCertified = true), null, now)

        assertEquals(now, inspection.checkedAt)
        assertNull(inspection.expiresAt)
        assertNull(inspection.failure)
    }

    @Test
    fun `several steps keep the expiry that bites first`() {
        val far = TlsInspection(expiresAt = now.plus(Duration.ofDays(90)), checkedAt = now.minusSeconds(30))
        val near = TlsInspection(expiresAt = now.plus(Duration.ofDays(5)), checkedAt = now, failure = "expires soon")

        val merged = far.merge(near)

        assertEquals(near.expiresAt, merged.expiresAt)
        assertEquals(now, merged.checkedAt)
        assertEquals("expires soon", merged.failure)
    }

    private fun certificate(
        daysUntilExpiration: Long,
        isExpiringSoon: Boolean,
    ) = SslCertificateInfo(
        daysUntilExpiration = daysUntilExpiration,
        expirationDate = now.plus(Duration.ofDays(daysUntilExpiration)),
        isExpiringSoon = isExpiringSoon,
    )

    private fun step(url: String = "https://example.test") = ProbeContent.HttpStep(name = "Request", url = url)

    private fun content(
        notificationCertified: Boolean = false,
        ignoreCertificateErrors: Boolean = false,
    ) = ProbeContent.Http(
        url = "https://example.test",
        notificationCertified = notificationCertified,
        ignoreCertificateErrors = ignoreCertificateErrors,
        httpCodeAllowed = listOf(HttpCodeEnum.OK),
    )

    private fun probe(tlsCheckedAt: Instant? = null) =
        ProbeDTO(
            id = UUID.randomUUID(),
            name = "HTTPS test",
            interval = 60,
            timeout = 5,
            retry = 1,
            intervalRetry = 1,
            enabled = true,
            protocol = ProbeProtocol.HTTP,
            description = null,
            lastRun = null,
            status = ProbeMonitorLogStatus.SUCCESS,
            content = content(),
            tlsCheckedAt = tlsCheckedAt,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )
}
