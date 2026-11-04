package tmenier.fr.schedulers.templates

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.schedulers.services.DockerContainerInspector
import tmenier.fr.schedulers.services.DockerContainerState

@ApplicationScoped
class ProbeProtocolDockerContainer(
    private val inspector: DockerContainerInspector,
) : ProbeProtocolAbstract<ProbeContent.DockerContainer>() {
    override fun execute(
        probe: ProbeDTO,
        content: ProbeContent.DockerContainer,
        isLastAttempt: Boolean,
    ): ProbeResult {
        val start = now()

        return try {
            val state = inspector.inspect(content, timeoutOf(probe))
            val responseTime = getResponseTime(start)
            val problem = problemOf(state)
            ProbeResult(
                status = if (problem == null) ProbeMonitorLogStatus.SUCCESS else failureStatus(isLastAttempt, probe),
                responseTime = responseTime,
                message = problem ?: "Container ${state.name} is running${state.health?.let { " ($it)" }.orEmpty()}",
                runAt = start,
            )
        } catch (error: Exception) {
            ProbeResult(
                status = failureStatus(isLastAttempt, probe),
                responseTime = getResponseTime(start),
                message = "Docker container check failed: ${error.message ?: error.javaClass.simpleName}",
                runAt = start,
            )
        }
    }

    override fun getProtocolType() = ProbeProtocol.DOCKER.name

    internal fun problemOf(state: DockerContainerState): String? {
        val name = state.name
        return when {
            state.status == "running" && (state.health == null || state.health == "healthy") -> null
            state.status == "running" && state.health == "starting" -> "Container $name is running but its health check is still starting"
            state.status == "running" -> "Container $name is ${state.health}"
            state.status == "restarting" -> "Container $name is restarting (restarted ${state.restartCount} times)"
            state.status == "exited" || state.status == "dead" -> {
                val details = listOfNotNull(state.exitCode?.let { "exit code $it" }, state.error).joinToString(", ")
                "Container $name is ${state.status}${if (details.isEmpty()) "" else " ($details)"}"
            }

            else -> "Container $name is ${state.status}"
        }
    }

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
