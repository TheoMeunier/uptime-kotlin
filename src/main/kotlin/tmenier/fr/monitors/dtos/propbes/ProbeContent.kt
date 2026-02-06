package tmenier.fr.monitors.dtos.propbes

import io.quarkus.runtime.annotations.RegisterForReflection
import tmenier.fr.monitors.enums.HttpCodeEnum

@RegisterForReflection
sealed interface ProbeContent {
    @RegisterForReflection
    data class Http(
        val url: String,
        val notificationCertified: Boolean,
        val ignoreCertificateErrors: Boolean,
        val httpCodeAllowed: List<HttpCodeEnum>,
    ) : ProbeContent

    @RegisterForReflection
    data class Dns(
        val hostname: String,
        val dnsPort: Int,
        val dnsServer: String,
    ) : ProbeContent

    @RegisterForReflection
    data class Ping(
        val ip: String,
        val pingMaxPacket: Int,
        val pingSize: Int,
        val pingDelay: Int,
        val pingNumericOutput: Boolean? = false,
    ) : ProbeContent

    @RegisterForReflection
    data class Tcp(
        val url: String,
        val tcpPort: Int,
    ) : ProbeContent
}
