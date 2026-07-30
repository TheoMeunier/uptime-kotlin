package tmenier.fr.monitors.requests

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.IpAddress
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.enums.monitors.HttpCodeEnum
import tmenier.fr.common.enums.monitors.HttpMethodEnum
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.common.enums.monitors.RecordDnsEnum
import tmenier.fr.common.validations.UrlOrIp
import java.util.UUID

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "protocol", visible = true)
@JsonSubTypes(
    JsonSubTypes.Type(value = ValidProbeProtocolHttpRequest::class, name = "HTTP"),
    JsonSubTypes.Type(value = ValidProbeProtocolTcpRequest::class, name = "TCP"),
    JsonSubTypes.Type(value = ValidProbeProtocolDnsRequest::class, name = "DNS"),
    JsonSubTypes.Type(value = ValidProbeProtocolPingRequest::class, name = "PING"),
    JsonSubTypes.Type(value = ValidProbeProtocolPostgreSqlRequest::class, name = "POSTGRESQL"),
    JsonSubTypes.Type(value = ValidProbeProtocolMySqlRequest::class, name = "MYSQL / MARIADB"),
)
@RegisterForReflection
abstract class BaseStoreProbeRequest {
    @field:NotBlank(message = "Name is required")
    lateinit var name: String

    @field:NotNull(message = "Interval is required")
    @field:Min(value = 1, message = "Interval must be at least 1 second")
    @field:Positive(message = "Interval must be greater than 0")
    var interval: Int? = null

    @field:NotNull(message = "Protocol is required")
    lateinit var protocol: ProbeProtocol

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

    @field:Size(min = 1, message = "At least one notification is required")
    val notifications: List<UUID> = emptyList()
}

@RegisterForReflection
data class ValidProbeProtocolHttpRequest(
    @field:NotBlank(message = "Url is required")
    @field:UrlOrIp(message = "Invalid URL or IP format")
    var url: String,
    val notificationCertificate: Boolean,
    val ignoreCertificateErrors: Boolean,
    @field:Size(min = 1, message = "At least one HTTP code is required")
    val httpCodeAllowed: List<HttpCodeEnum> = emptyList(),
    val method: HttpMethodEnum = HttpMethodEnum.GET,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null,
    val authentication: ProbeContent.HttpAuthentication? = null,
    val assertions: List<ProbeContent.HttpAssertion> = emptyList(),
    val followRedirects: Boolean = true,
    @field:Positive(message = "Maximum latency must be greater than 0")
    val maxLatencyMs: Long? = null,
    @field:Min(7)
    @field:Max(30)
    val tlsExpiryWarningDays: Int = 30,
    val steps: List<ProbeContent.HttpStep> = emptyList(),
) : BaseStoreProbeRequest() {
    @AssertTrue(message = "TLS expiry warning must be 30, 15, or 7 days")
    @JsonIgnore
    fun isTlsExpiryWarningValid(): Boolean = tlsExpiryWarningDays in setOf(30, 15, 7)
}

@RegisterForReflection
data class ValidProbeProtocolTcpRequest(
    @field:NotBlank(message = "Url is required")
    @field:UrlOrIp(message = "Invalid URL or IP format")
    var url: String,
    @field:Min(1)
    @field:Max(65535)
    val tcpPort: Int,
) : BaseStoreProbeRequest()

@RegisterForReflection
data class ValidProbeProtocolDnsRequest(
    @field:NotBlank(message = "Url is required")
    @field:UrlOrIp(message = "Invalid URL or IP format")
    var hostname: String,
    @field:Min(1)
    @field:Max(65535)
    val dnsPort: Int,
    @field:IpAddress()
    val dnsServer: String,
    val recordType: RecordDnsEnum,
) : BaseStoreProbeRequest()

@RegisterForReflection
data class ValidProbeProtocolPingRequest(
    @field:NotBlank(message = "Url is required")
    @field:UrlOrIp(message = "Invalid URL or IP format")
    var ip: String,
    @field:Min(1)
    @field:Max(60)
    val pingHeartbeatInterval: Int,
    @field:Min(1)
    @field:Max(10)
    val pingMaxPacket: Int,
    @field:Min(32)
    @field:Max(65500)
    val pingSize: Int,
    @field:Min(1)
    @field:Max(60)
    val pingDelay: Int,
    val pingNumericOutput: Boolean,
) : BaseStoreProbeRequest()

@RegisterForReflection
data class ValidProbeProtocolPostgreSqlRequest(
    @field:NotBlank(message = "PostgreSQL connection string is required")
    @field:Pattern(
        regexp = "postgres(?:ql)?://[^\\s]+",
        message = "Invalid PostgreSQL connection string",
    )
    val connectionString: String,
    @field:NotBlank(message = "PostgreSQL query is required")
    val query: String = "SELECT 1",
) : BaseStoreProbeRequest()

@RegisterForReflection
data class ValidProbeProtocolMySqlRequest(
    @field:NotBlank(message = "MySQL/MariaDB connection string is required")
    @field:Pattern(
        regexp = "(?:mysql|mariadb)://[^\\s]+",
        message = "Invalid MySQL/MariaDB connection string",
    )
    val connectionString: String,
    @field:NotBlank(message = "MySQL/MariaDB query is required")
    val query: String = "SELECT 1",
) : BaseStoreProbeRequest()
