package tmenier.fr.monitors.schedulers.templates

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.monitors.enums.HttpCodeEnum
import tmenier.fr.monitors.enums.ProbeProtocol
import tmenier.fr.monitors.entities.ProbesEntity
import tmenier.fr.monitors.enums.ProbeMonitorLogStatus
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
class ProbeProtocolHttp : ProbeProtocolAbstract() {

    override fun execute(probe: ProbesEntity, isFailed: Boolean?): ProbeResult {
        val start = now()

        return try {
            val clientBuilder = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))

            // skip error certificate
            if (probe.ignoreCertificateErrors) {
                clientBuilder.sslContext(createInsecureSSLContext())
            }

            val client = clientBuilder.build()

            val request = HttpRequest.newBuilder()
                .uri(URI(probe.url))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            val success = checkIfStatusCodeIsValid(probe.httpCodeAllowed, response.statusCode())

            ProbeResult(
                status = if (success) ProbeMonitorLogStatus.SUCCESS else ProbeMonitorLogStatus.WARNING,
                responseTime = getResponseTime(start),
                message = "HTTP Status: ${response.statusCode()} in ${getResponseTime(start)} ms",
                runAt =  getRunAt(start)
            )
        } catch (e: Exception) {
            ProbeResult(
                status = if (isFailed == true) ProbeMonitorLogStatus.FAILURE else ProbeMonitorLogStatus.WARNING,
                responseTime = getResponseTime(start),
                message = "HTTP request failed: ${e.message}",
                runAt = getRunAt(start)
            )
        }
    }

    private fun createInsecureSSLContext(): SSLContext {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        return SSLContext.getInstance("TLS").apply {
            init(null, trustAllCerts, SecureRandom())
        }
    }

    private fun checkIfStatusCodeIsValid(httpCodeAllowed: List<HttpCodeEnum>, statusCode: Int): Boolean {
        return httpCodeAllowed.any { allowedCode ->
            when {
                allowedCode.value.contains("-") -> {
                    val (start, end) = allowedCode.value.split("-").map { it.toInt() }
                    statusCode in start..end
                }
                else -> allowedCode.value.toInt() == statusCode
            }
        }
    }

    override fun getProtocolType() = ProbeProtocol.HTTP.name
}