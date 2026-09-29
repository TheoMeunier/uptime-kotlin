package tmenier.fr.schedulers.services

import jakarta.enterprise.context.ApplicationScoped
import java.net.http.HttpClient
import java.time.Duration

fun interface ProbeHttpClientFactory {
    fun create(
        followRedirects: Boolean,
        ignoreCertificateErrors: Boolean,
        connectTimeout: Duration,
    ): HttpClient
}

@ApplicationScoped
class DefaultProbeHttpClientFactory(
    private val sslCertificateService: SslCertificateService,
) : ProbeHttpClientFactory {
    override fun create(
        followRedirects: Boolean,
        ignoreCertificateErrors: Boolean,
        connectTimeout: Duration,
    ): HttpClient {
        val builder =
            HttpClient
                .newBuilder()
                .connectTimeout(connectTimeout)
                .followRedirects(if (followRedirects) HttpClient.Redirect.NORMAL else HttpClient.Redirect.NEVER)
        if (ignoreCertificateErrors) {
            builder.sslContext(sslCertificateService.createInsecureSSLContext())
        }
        return builder.build()
    }
}
