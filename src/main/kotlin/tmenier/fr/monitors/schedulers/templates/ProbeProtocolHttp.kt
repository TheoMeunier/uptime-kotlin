package tmenier.fr.monitors.schedulers.templates

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.monitors.dtos.propbes.ProbeContent
import tmenier.fr.monitors.entities.ProbesEntity
import tmenier.fr.monitors.entities.mapper.ProbeContentMapper
import tmenier.fr.monitors.enums.HttpCodeEnum
import tmenier.fr.monitors.enums.ProbeMonitorLogStatus
import tmenier.fr.monitors.enums.ProbeProtocol
import tmenier.fr.monitors.schedulers.dto.ProbeResult
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.time.Duration
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@ApplicationScoped
class ProbeProtocolHttp : ProbeProtocolAbstract<ProbeContent.Http>() {
    override fun execute(
        probe: ProbesEntity,
        content: ProbeContent.Http,
        isLastAttempt: Boolean,
    ): ProbeResult {
        val start = now()
        val probeContent = ProbeContentMapper.toDto(probe)

        return try {
            val clientBuilder =
                HttpClient
                    .newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))

            // skip error certificate
            if (content.ignoreCertificateErrors) {
                clientBuilder.sslContext(createInsecureSSLContext())
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

            ProbeResult(
                status = getStatus(success, isLastAttempt, probe),
                responseTime = getResponseTime(start),
                message = "HTTP Status: ${response.statusCode()} in ${getResponseTime(start)} ms",
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

    private fun createInsecureSSLContext(): SSLContext {
        val trustAllCerts =
            arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun checkClientTrusted(
                        chain: Array<X509Certificate>,
                        authType: String,
                    ) {
                    }

                    override fun checkServerTrusted(
                        chain: Array<X509Certificate>,
                        authType: String,
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                },
            )

        return SSLContext.getInstance("TLS").apply {
            init(null, trustAllCerts, SecureRandom())
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
