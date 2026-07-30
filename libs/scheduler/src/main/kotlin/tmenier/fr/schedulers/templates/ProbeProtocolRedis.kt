package tmenier.fr.schedulers.templates

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.schedulers.services.RedisHealthCheck

@ApplicationScoped
class ProbeProtocolRedis(
    private val healthCheck: RedisHealthCheck,
) : ProbeProtocolAbstract<ProbeContent.Redis>() {
    override fun execute(
        probe: ProbeDTO,
        content: ProbeContent.Redis,
        isLastAttempt: Boolean,
    ): ProbeResult {
        val start = now()

        return try {
            healthCheck.check(content, CONNECTION_TIMEOUT_SECONDS)
            val responseTime = getResponseTime(start)
            ProbeResult(
                status = ProbeMonitorLogStatus.SUCCESS,
                responseTime = responseTime,
                message = "Redis command successful in ${responseTime}ms",
                runAt = getRunAt(start),
            )
        } catch (error: Exception) {
            ProbeResult(
                status = failureStatus(isLastAttempt, probe),
                responseTime = getResponseTime(start),
                message = "Redis command failed: ${error.message}",
                runAt = getRunAt(start),
            )
        }
    }

    override fun getProtocolType() = ProbeProtocol.REDIS.name

    private fun failureStatus(
        isLastAttempt: Boolean,
        probe: ProbeDTO,
    ): ProbeMonitorLogStatus =
        if (isLastAttempt || probe.status == ProbeMonitorLogStatus.FAILURE) {
            ProbeMonitorLogStatus.FAILURE
        } else {
            ProbeMonitorLogStatus.WARNING
        }

    private companion object {
        const val CONNECTION_TIMEOUT_SECONDS = 5
    }
}
