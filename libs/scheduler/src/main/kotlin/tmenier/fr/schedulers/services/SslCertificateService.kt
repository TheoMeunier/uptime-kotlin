package tmenier.fr.schedulers.services

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.schedulers.dto.SslCertificateInfo
import java.net.InetSocketAddress
import java.net.URI
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@ApplicationScoped
class SslCertificateService {
    fun createInsecureSSLContext(): SSLContext {
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

    fun checkSslCertificate(
        url: String,
        warningDays: Int = 30,
    ): SslCertificateInfo? {
        return try {
            val uri = URI(url)
            val host = uri.host
            val port = if (uri.port == -1) 443 else uri.port

            val sslSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
            val socket = sslSocketFactory.createSocket() as SSLSocket

            socket.use {
                it.connect(InetSocketAddress(host, port), 5000)
                it.startHandshake()

                val cert =
                    it.session.peerCertificates
                        .filterIsInstance<X509Certificate>()
                        .firstOrNull() ?: return null

                val expirationDate = cert.notAfter.toInstant()
                val dayUntilExpiration = ChronoUnit.DAYS.between(Instant.now(), expirationDate)

                SslCertificateInfo(
                    daysUntilExpiration = dayUntilExpiration,
                    expirationDate = expirationDate,
                    isExpiringSoon = dayUntilExpiration <= warningDays,
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    fun buildSslWarningMessage(sslInfo: SslCertificateInfo): String? =
        when {
            sslInfo.daysUntilExpiration <= 0 -> {
                "SSL certificate expired on ${-sslInfo.daysUntilExpiration} days"
            }

            sslInfo.isExpiringSoon -> {
                "SSL certificate expires in ${sslInfo.daysUntilExpiration} days (${sslInfo.expirationDate})"
            }

            else -> {
                null
            }
        }
}
