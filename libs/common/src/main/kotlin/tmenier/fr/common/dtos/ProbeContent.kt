package tmenier.fr.common.dtos

import io.quarkus.runtime.annotations.RegisterForReflection
import tmenier.fr.common.enums.monitors.HttpCodeEnum
import tmenier.fr.common.enums.monitors.HttpMethodEnum
import tmenier.fr.common.enums.monitors.RecordDnsEnum
import tmenier.fr.common.enums.monitors.SmtpSecurity

@RegisterForReflection
sealed interface ProbeContent {
    @RegisterForReflection
    data class Http(
        val url: String,
        val notificationCertified: Boolean,
        val ignoreCertificateErrors: Boolean,
        val httpCodeAllowed: List<HttpCodeEnum>,
        val method: HttpMethodEnum = HttpMethodEnum.GET,
        val headers: Map<String, String> = emptyMap(),
        val body: String? = null,
        val authentication: HttpAuthentication? = null,
        val assertions: List<HttpAssertion> = emptyList(),
        val followRedirects: Boolean = true,
        val maxLatencyMs: Long? = null,
        val tlsExpiryWarningDays: Int = 30,
        val steps: List<HttpStep> = emptyList(),
    ) : ProbeContent

    @RegisterForReflection
    data class HttpStep(
        val name: String,
        val url: String,
        val method: HttpMethodEnum = HttpMethodEnum.GET,
        val headers: Map<String, String> = emptyMap(),
        val body: String? = null,
        val authentication: HttpAuthentication? = null,
        val httpCodeAllowed: List<HttpCodeEnum> = emptyList(),
        val assertions: List<HttpAssertion> = emptyList(),
        val followRedirects: Boolean? = null,
        val maxLatencyMs: Long? = null,
    )

    @RegisterForReflection
    data class HttpAuthentication(
        val type: HttpAuthenticationType,
        val username: String? = null,
        val password: String? = null,
        val token: String? = null,
    )

    enum class HttpAuthenticationType { BASIC, BEARER }

    @RegisterForReflection
    data class HttpAssertion(
        val type: HttpAssertionType,
        val expected: String,
        val path: String? = null,
        val header: String? = null,
    )

    enum class HttpAssertionType { TEXT_CONTAINS, JSON_EQUALS, RESPONSE_HEADER_EQUALS }

    @RegisterForReflection
    data class Dns(
        val hostname: String,
        val dnsPort: Int,
        val dnsServer: String,
        val recordType: RecordDnsEnum? = null,
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

    @RegisterForReflection
    data class PostgreSql(
        val connectionString: String,
        val host: String,
        val query: String = "SELECT 1",
    ) : ProbeContent

    @RegisterForReflection
    data class SqlServer(
        val connectionString: String,
        val host: String,
        val query: String = "SELECT 1",
    ) : ProbeContent

    @RegisterForReflection
    data class MySql(
        val connectionString: String,
        val host: String,
        val query: String = "SELECT 1",
    ) : ProbeContent

    @RegisterForReflection
    data class Redis(
        val connectionString: String,
        val host: String,
        val command: String = "PING",
    ) : ProbeContent

    @RegisterForReflection
    data class Smtp(
        val hostname: String,
        val port: Int,
        val security: SmtpSecurity,
    ) : ProbeContent

    @RegisterForReflection
    data class Kafka(
        val brokers: String,
        val topic: String,
        val message: String,
        val ssl: Boolean = false,
        val allowAutoTopicCreation: Boolean = false,
    ) : ProbeContent
}
