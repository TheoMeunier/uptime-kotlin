package tmenier.fr.monitors.dtos.requests

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.IpAddress
import tmenier.fr.monitors.enums.HttpCodeEnum
import tmenier.fr.monitors.enums.ProbeProtocol
import tmenier.fr.common.validations.UrlOrIp

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "protocol", visible = true)
@JsonSubTypes(
    JsonSubTypes.Type(value = ValidProbeProtocolHttpRequest::class, name = "HTTP"),
    JsonSubTypes.Type(value = ValidProbeProtocolTcpRequest::class, name = "TCP"),
    JsonSubTypes.Type(value = ValidProbeProtocolDnsRequest::class, name = "DNS"),
    JsonSubTypes.Type(value = ValidProbeProtocolPingRequest::class, name = "PING")
)

@RegisterForReflection
abstract class BaseStoreProbeRequest {
    @field:NotBlank(message = "Name is required")
    var name: String? = null

    @field:NotNull(message = "Interval is required")
    @field:Min(value = 1, message = "Interval must be at least 1 second")
    @field:Positive(message = "Interval must be greater than 0")
    var interval: Int? = null

    @field:NotNull(message = "Protocol is required")
    var protocol: ProbeProtocol? = null

    @field:NotNull(message = "Retry is required")
    @field:Min(value = 1, message = "Retry must be at least 1")
    @field:Max(value = 10, message = "Retry must be at most 10")
    @field:Positive(message = "Retry must be greater than 0")
    var retry: Int? = null

    @field:NotNull(message = "Interval Retry is required")
    @field:Min(value = 1, message = "Interval Retry must be at least 1 second")
    @field:Positive(message = "Interval Retry must be greater than 0")
    var intervalRetry: Int? = null

    @field:NotNull(message = "Active is required")
    var enabled: Boolean? = false

    @field:Size(min = 10, message = "Description must be at least 10 characters long")
    var description: String? = null

    @field:NotBlank(message = "Url is required")
    @field:UrlOrIp(message = "Invalid URL or IP format")
    var url: String? = null
}

@RegisterForReflection
data class ValidProbeProtocolHttpRequest(
    val notificationCertificate: Boolean? = false,
    val ignoreCertificateErrors: Boolean? = false,

    @field:Valid
    @field:Size(min = 1, message = "At least one HTTP code is required")
    val httpCodeAllowed: List<HttpCodeEnum> = emptyList()
) : BaseStoreProbeRequest()

@RegisterForReflection
data class ValidProbeProtocolTcpRequest(
    @field:Min(1)
    @field:Max(65535)
    val tcpPort: Int? = null
) : BaseStoreProbeRequest()

@RegisterForReflection
data class ValidProbeProtocolDnsRequest(
    @field:Min(1)
    @field:Max(65535)
    val dnsPort: Int,

    @field:IpAddress()
    val dnsServer: String
) : BaseStoreProbeRequest()

@RegisterForReflection
data class ValidProbeProtocolPingRequest(
    @field:Min(1)
    @field:Max(60)
    val pingHeartbeatInterval: Int? = null,

    @field:Min(1)
    @field:Max(10)
    val pingMaxPacket: Int? = null,

    @field:Min(32)
    @field:Max(65500)
    val pingSize: Int? = null,

    @field:Min(1)
    @field:Max(60)
    val pingDelay: Int? = null,

    val pingNumericOutput: Boolean? = false
) : BaseStoreProbeRequest()