package tmenier.fr.schedulers.services

import jakarta.enterprise.context.ApplicationScoped
import java.net.http.HttpClient
import java.time.Duration

fun interface ProbeHttpClientFactory {
    fun create(
        followRedirects: Boolean,
        ignoreCertificateErrors: Boolean,
    ): HttpClient
}

@ApplicationScoped
class DefaultProbeHttpClientFactory(
    private val sslCertificateService: SslCertificateService,
) : ProbeHttpClientFactory {
    override fun create(
        followRedirects: Boolean,
        ignoreCertificateErrors: Boolean,
    ): HttpClient {
        val builder =
            HttpClient
                .newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(if (followRedirects) HttpClient.Redirect.NORMAL else HttpClient.Redirect.NEVER)
        if (ignoreCertificateErrors) {
            builder.sslContext(sslCertificateService.createInsecureSSLContext())
        }
        return builder.build()
    }

    private companion object {
        val CONNECT_TIMEOUT: Duration = Duration.ofSeconds(5)
    }
}
