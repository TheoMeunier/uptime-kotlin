package tmenier.fr.monitors.schedulers.templates

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.monitors.enums.HttpCodeEnum
import tmenier.fr.monitors.enums.ProbeProtocol
import tmenier.fr.monitors.entities.ProbesEntity
import tmenier.fr.monitors.schedulers.ProbeSchedulerInterface
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
class ProbeProtocolHttp : ProbeSchedulerInterface {

    override fun execute(probe: ProbesEntity): ProbeResult {
        val startTime = System.currentTimeMillis()

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
            val responseTime = System.currentTimeMillis() - startTime
            val success = checkIfStatusCodeIsValid(probe.httpCodeAllowed, response.statusCode())

            ProbeResult(
                success = success,
                responseTime = responseTime,
                message = "HTTP Status: ${response.statusCode()} in ${responseTime.toString()}",
                statusCode = response.statusCode(),
                responseBody = response.body()?.take(500)
            )
        } catch (e: Exception) {
            val responseTime = System.currentTimeMillis() - startTime

            ProbeResult(
                success = false,
                responseTime = responseTime,
                message = "Erreur HTTP",
                error = e.message
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
        return httpCodeAllowed.contains(HttpCodeEnum.fromValue(statusCode.toString()))
    }

    override fun getProtocolType() = ProbeProtocol.HTTP.name
}