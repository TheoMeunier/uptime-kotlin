package tmenier.fr.monitors.schedulers.dto

data class ProbeResult(
    val success: Boolean,
    val responseTime: Long,
    val message: String,
    var error: String? = null,
    val statusCode: Int? = null,
    val responseBody: String? = null
)