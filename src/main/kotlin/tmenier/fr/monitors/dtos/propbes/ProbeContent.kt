package tmenier.fr.monitors.dtos.propbes

import tmenier.fr.monitors.enums.HttpCodeEnum

sealed interface ProbeContent {

    data class Http(
        val url: String,
        val notificationCertified: Boolean,
        val ignoreCertificateErrors: Boolean,
        val httpCodeAllowed: List<HttpCodeEnum>,
    ) : ProbeContent

    data class Dns(
        val hostname: String,
        val dnsPort: Int,
        val dnsServer: String,
    ) : ProbeContent

    data class Ping(
        val ip: String,
        val pingMaxPacket: Int,
        val pingSize: Int,
        val pingDelay: Int,
        val pingNumericOutput: Boolean? = false,
    ) : ProbeContent

    data class Tcp(
        val url: String,
        val tcpPort: Int,
    ) : ProbeContent
}