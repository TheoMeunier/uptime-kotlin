package tmenier.fr.monitors.dtos.responses

import tmenier.fr.monitors.enums.ProbeMonitorLogStatus
import java.util.UUID

data class ProbeListDTO(
    val id: UUID,
    val name: String,
    val description: String? = null,
    val status: ProbeMonitorLogStatus
)

data class ProbeDTO(
    val id: String,
    val name: String,
    val url: String,
    val interval: Int,
    val timeout: Int,
    val retry: Int,
    val intervalRetry: Int,
    val enabled: Boolean,
    val protocol: String,
    val description: String?,
    val lastRun: String?,
    val notificationCertified: Boolean,
    val ignoreCertificateErrors: Boolean,
    val httpCodeAllowed: List<String>,
    val tcpPort: Int?,
    val dnsPort: Int?,
    val dnsServer: String?,
    val pingMaxPacket: Int?,
    val pingSize: Int?,
    val pingDelay: Int?,
    val pingNumericOutput: Boolean?,
    val createdAt: String,
    val updatedAt: String,
)