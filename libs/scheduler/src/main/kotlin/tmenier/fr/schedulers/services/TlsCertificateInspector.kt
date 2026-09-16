package tmenier.fr.schedulers.services

import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.dtos.SslCertificateInfo
import tmenier.fr.common.utils.MonitoringClock
import tmenier.fr.databases.dtos.ProbeDTO
import java.time.Duration
import java.time.Instant

@RegisterForReflection
data class TlsInspection(
    val expiresAt: Instant? = null,
    val checkedAt: Instant? = null,
    val failure: String? = null,
) {
    fun merge(other: TlsInspection) =
        TlsInspection(
            expiresAt = listOfNotNull(expiresAt, other.expiresAt).minOrNull(),
            checkedAt = listOfNotNull(checkedAt, other.checkedAt).maxOrNull(),
            failure = failure ?: other.failure,
        )

    companion object {
        val NONE = TlsInspection()
    }
}


@ApplicationScoped
class TlsCertificateInspector(
    private val sslCertificateService: SslCertificateService,
) {
    fun inspect(
        probe: ProbeDTO,
        content: ProbeContent.Http,
        step: ProbeContent.HttpStep,
    ): TlsInspection {
        if (!isInspectionDue(probe, content, step)) return TlsInspection.NONE

        val checkedAt = MonitoringClock.now()

        return interpret(
            content = content,
            certificate = sslCertificateService.checkSslCertificate(step.url, content.tlsExpiryWarningDays),
            checkedAt = checkedAt,
        )
    }

    fun isInspectionDue(
        probe: ProbeDTO,
        content: ProbeContent.Http,
        step: ProbeContent.HttpStep,
        now: Instant = MonitoringClock.now(),
    ): Boolean {
        if (!step.url.startsWith("https://", ignoreCase = true)) return false
        if (content.ignoreCertificateErrors) return false

        return content.notificationCertified || isRefreshDue(probe.tlsCheckedAt, now)
    }

    fun interpret(
        content: ProbeContent.Http,
        certificate: SslCertificateInfo?,
        checkedAt: Instant,
    ): TlsInspection {
        if (certificate == null) return TlsInspection(checkedAt = checkedAt)

        return TlsInspection(
            expiresAt = certificate.expirationDate,
            checkedAt = checkedAt,
            failure =
                if (content.notificationCertified && certificate.isExpiringSoon) {
                    sslCertificateService.buildSslWarningMessage(certificate) ?: "TLS certificate expires soon"
                } else {
                    null
                },
        )
    }

    private fun isRefreshDue(
        lastCheckedAt: Instant?,
        now: Instant,
    ): Boolean = lastCheckedAt == null || lastCheckedAt.isBefore(now.minus(TLS_REFRESH_INTERVAL))

    private companion object {
        val TLS_REFRESH_INTERVAL: Duration = Duration.ofHours(24)
    }
}
