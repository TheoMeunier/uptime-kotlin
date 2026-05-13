package tmenier.fr.schedulers.templates

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.monitors.dtos.propbes.ProbeContent
import tmenier.fr.monitors.entities.ProbesEntity
import tmenier.fr.monitors.enums.HttpCodeEnum
import tmenier.fr.monitors.enums.ProbeMonitorLogStatus
import tmenier.fr.monitors.enums.ProbeProtocol
import tmenier.fr.monitors.services.SslCertificateService
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

@ApplicationScoped
class ProbeProtocolHttp(
    private val sslCertificateService: SslCertificateService,
) : ProbeProtocolAbstract<ProbeContent.Http>() {
    override fun execute(
        probe: ProbesEntity,
        content: ProbeContent.Http,
        isLastAttempt: Boolean,
    ): ProbeResult {
        val start = now()

        return try {
            val clientBuilder =
                HttpClient
                    .newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))

            // skip error certificate
            if (content.ignoreCertificateErrors) {
                clientBuilder.sslContext(sslCertificateService.createInsecureSSLContext())
            }

            val client = clientBuilder.build()

            val request =
                HttpRequest
                    .newBuilder()
                    .uri(URI(content.url))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            val success = checkIfStatusCodeIsValid(content.httpCodeAllowed, response.statusCode())

            // check ssl certificate
            val sslInfo =
                if (content.url.startsWith("https://") && content.notificationCertified && !content.ignoreCertificateErrors) {
                    sslCertificateService.checkSslCertificate(content.url)
                } else {
                    null
                }

            val sslWarning = sslInfo?.let { sslCertificateService.buildSslWarningMessage(it) }
            val finalSuccess = success && (sslInfo == null || !sslInfo.isExpiringSoon)
            val message =
                buildString {
                    append("HTTP Status: ${response.statusCode()} in ${getResponseTime(start)} ms")
                    if (sslWarning != null) append(" - $sslWarning")
                }

            ProbeResult(
                status = getStatus(finalSuccess, isLastAttempt, probe),
                responseTime = getResponseTime(start),
                message = message,
                runAt = getRunAt(start),
            )
        } catch (e: Exception) {
            ProbeResult(
                status = getStatus(false, isLastAttempt, probe),
                responseTime = getResponseTime(start),
                message = "HTTP request failed: ${e.message}",
                runAt = getRunAt(start),
            )
        }
    }

    private fun checkIfStatusCodeIsValid(
        httpCodeAllowed: List<HttpCodeEnum>,
        statusCode: Int,
    ): Boolean =
        httpCodeAllowed.any { allowedCode ->
            when {
                allowedCode.value.contains("-") -> {
                    val (start, end) = allowedCode.value.split("-").map { it.toInt() }
                    statusCode in start..end
                }

                else -> {
                    allowedCode.value.toInt() == statusCode
                }
            }
        }

    private fun getStatus(
        isSuccess: Boolean,
        isLastAttempt: Boolean,
        probe: ProbesEntity,
    ): ProbeMonitorLogStatus {
        if (isSuccess) {
            return ProbeMonitorLogStatus.SUCCESS
        }

        if (isLastAttempt) {
            return ProbeMonitorLogStatus.FAILURE
        }

        if (probe.status == ProbeMonitorLogStatus.FAILURE) {
            return ProbeMonitorLogStatus.FAILURE
        }

        return ProbeMonitorLogStatus.WARNING
    }

    override fun getProtocolType() = ProbeProtocol.HTTP.name
}
