package tmenier.fr.schedulers.templates

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.schedulers.services.SqlServerHealthCheck

@ApplicationScoped
class ProbeProtocolSqlServer(
    private val healthCheck: SqlServerHealthCheck,
) : ProbeProtocolAbstract<ProbeContent.SqlServer>() {
    override fun execute(
        probe: ProbeDTO,
        content: ProbeContent.SqlServer,
        isLastAttempt: Boolean,
    ): ProbeResult {
        val start = now()

        return try {
            healthCheck.check(content, CONNECTION_TIMEOUT_SECONDS)
            val responseTime = getResponseTime(start)
            ProbeResult(
                status = ProbeMonitorLogStatus.SUCCESS,
                responseTime = responseTime,
                message = "Microsoft SQL Server connection successful in ${responseTime}ms",
                runAt = getRunAt(start),
            )
        } catch (error: Exception) {
            ProbeResult(
                status = failureStatus(isLastAttempt, probe),
                responseTime = getResponseTime(start),
                message = "Microsoft SQL Server connection failed: ${error.message}",
                runAt = getRunAt(start),
            )
        }
    }

    override fun getProtocolType() = ProbeProtocol.SQLSERVER.name

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
