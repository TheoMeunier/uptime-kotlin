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
            healthCheck.check(content, timeoutSeconds(probe))
            val responseTime = getResponseTime(start)
            ProbeResult(
                status = ProbeMonitorLogStatus.SUCCESS,
                responseTime = responseTime,
                message = "Redis command successful in ${responseTime}ms",
                runAt = start,
            )
        } catch (error: Exception) {
            ProbeResult(
                status = failureStatus(isLastAttempt, probe),
                responseTime = getResponseTime(start),
                message = "Redis command failed: ${error.message}",
                runAt = start,
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
}
