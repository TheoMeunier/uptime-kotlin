package tmenier.fr.schedulers.templates

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.enums.monitors.HttpCodeEnum
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.schedulers.services.SslCertificateService
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.Base64

@ApplicationScoped
class ProbeProtocolHttp(
    private val sslCertificateService: SslCertificateService,
    private val objectMapper: ObjectMapper,
) : ProbeProtocolAbstract<ProbeContent.Http>() {
    override fun execute(
        probe: ProbeDTO,
        content: ProbeContent.Http,
        isLastAttempt: Boolean,
    ): ProbeResult {
        val startedAt = now()

        return try {
            val steps = content.steps.ifEmpty { listOf(content.asSingleStep()) }
            val messages = mutableListOf<String>()
            var lastStatusCode: Int? = null
            var lastBody: String? = null

            for ((index, step) in steps.withIndex()) {
                val stepStartedAt = now()
                val response = executeStep(content, step)
                val latency = getResponseTime(stepStartedAt)
                lastStatusCode = response.statusCode()
                lastBody = response.body()

                val allowedCodes = step.httpCodeAllowed.ifEmpty { content.httpCodeAllowed }
                require(checkIfStatusCodeIsValid(allowedCodes, response.statusCode())) {
                    "${step.name}: unexpected HTTP status ${response.statusCode()}"
                }
                val maxLatency = step.maxLatencyMs ?: content.maxLatencyMs
                require(maxLatency == null || latency <= maxLatency) {
                    "${step.name}: latency ${latency}ms exceeds ${maxLatency}ms"
                }
                checkAssertions(step.name, step.assertions.ifEmpty { content.assertions }, response)
                checkTls(content, step)
                messages += "${index + 1}/${steps.size} ${step.name}: HTTP ${response.statusCode()} in $latency ms"
            }

            ProbeResult(
                status = getStatus(true, isLastAttempt, probe),
                responseTime = getResponseTime(startedAt),
                message = messages.joinToString("; "),
                runAt = getRunAt(startedAt),
                statusCode = lastStatusCode,
                responseBody = lastBody,
            )
        } catch (e: Exception) {
            ProbeResult(
                status = getStatus(false, isLastAttempt, probe),
                responseTime = getResponseTime(startedAt),
                message = "HTTP check failed: ${e.message}",
                runAt = getRunAt(startedAt),
            )
        }
    }

    private fun executeStep(
        content: ProbeContent.Http,
        step: ProbeContent.HttpStep,
    ): HttpResponse<String> {
        val clientBuilder =
            HttpClient
                .newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(
                    if (step.followRedirects ?: content.followRedirects) {
                        HttpClient.Redirect.NORMAL
                    } else {
                        HttpClient.Redirect.NEVER
                    },
                )
        if (content.ignoreCertificateErrors) {
            clientBuilder.sslContext(sslCertificateService.createInsecureSSLContext())
        }

        val request = HttpRequest.newBuilder().uri(URI(step.url)).timeout(Duration.ofSeconds(5))
        (content.headers + step.headers).forEach(request::header)
        applyAuthentication(request, step.authentication ?: content.authentication)
        val publisher = step.body?.let(HttpRequest.BodyPublishers::ofString) ?: HttpRequest.BodyPublishers.noBody()
        request.method(step.method.name, publisher)
        return clientBuilder.build().send(request.build(), HttpResponse.BodyHandlers.ofString())
    }

    private fun applyAuthentication(
        request: HttpRequest.Builder,
        authentication: ProbeContent.HttpAuthentication?,
    ) {
        when (authentication?.type) {
            ProbeContent.HttpAuthenticationType.BASIC -> {
                require(!authentication.username.isNullOrBlank() && authentication.password != null) {
                    "Basic authentication requires a username and password"
                }
                val value = "${authentication.username}:${authentication.password}"
                val encoded = Base64.getEncoder().encodeToString(value.toByteArray(StandardCharsets.UTF_8))
                request.header("Authorization", "Basic $encoded")
            }
            ProbeContent.HttpAuthenticationType.BEARER -> {
                require(!authentication.token.isNullOrBlank()) { "Bearer authentication requires a token" }
                request.header("Authorization", "Bearer ${authentication.token}")
            }
            null -> Unit
        }
    }

    private fun checkAssertions(
        stepName: String,
        assertions: List<ProbeContent.HttpAssertion>,
        response: HttpResponse<String>,
    ) {
        assertions.forEach { assertion ->
            val matches =
                when (assertion.type) {
                    ProbeContent.HttpAssertionType.TEXT_CONTAINS -> response.body().contains(assertion.expected)
                    ProbeContent.HttpAssertionType.RESPONSE_HEADER_EQUALS ->
                        assertion.header?.let { response.headers().firstValue(it).orElse(null) } == assertion.expected
                    ProbeContent.HttpAssertionType.JSON_EQUALS -> {
                        val path = requireNotNull(assertion.path) { "JSON assertion requires a path" }
                        val root = objectMapper.readTree(response.body())
                        val pointer = if (path.startsWith("/")) path else "/${path.replace('.', '/')}"
                        root.at(pointer).let { node -> !node.isMissingNode && node.asText() == assertion.expected }
                    }
                }
            require(matches) { "$stepName: ${assertion.type} assertion failed" }
        }
    }

    private fun checkTls(
        content: ProbeContent.Http,
        step: ProbeContent.HttpStep,
    ) {
        if (!step.url.startsWith("https://") || !content.notificationCertified || content.ignoreCertificateErrors) return
        val sslInfo = sslCertificateService.checkSslCertificate(step.url, content.tlsExpiryWarningDays) ?: return
        require(!sslInfo.isExpiringSoon) { sslCertificateService.buildSslWarningMessage(sslInfo) ?: "TLS certificate expires soon" }
    }

    private fun ProbeContent.Http.asSingleStep() =
        ProbeContent.HttpStep(
            name = "Request",
            url = url,
            method = method,
            headers = headers,
            body = body,
            authentication = authentication,
            httpCodeAllowed = httpCodeAllowed,
            assertions = assertions,
            followRedirects = followRedirects,
            maxLatencyMs = maxLatencyMs,
        )

    private fun checkIfStatusCodeIsValid(
        httpCodeAllowed: List<HttpCodeEnum>,
        statusCode: Int,
    ): Boolean =
        httpCodeAllowed.any { allowedCode ->
            if (allowedCode.value.contains("-")) {
                val (start, end) = allowedCode.value.split("-").map(String::toInt)
                statusCode in start..end
            } else {
                allowedCode.value.toInt() == statusCode
            }
        }

    private fun getStatus(
        isSuccess: Boolean,
        isLastAttempt: Boolean,
        probe: ProbeDTO,
    ): ProbeMonitorLogStatus =
        when {
            isSuccess -> ProbeMonitorLogStatus.SUCCESS
            isLastAttempt || probe.status == ProbeMonitorLogStatus.FAILURE -> ProbeMonitorLogStatus.FAILURE
            else -> ProbeMonitorLogStatus.WARNING
        }

    override fun getProtocolType() = ProbeProtocol.HTTP.name
}
