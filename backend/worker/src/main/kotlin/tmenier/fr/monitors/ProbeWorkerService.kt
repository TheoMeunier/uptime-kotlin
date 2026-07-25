package tmenier.fr.monitors

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.dtos.ProbeCheckTaskDto
import tmenier.fr.databases.mappers.ProbeMapper
import tmenier.fr.databases.repositories.ProbeRepository
import tmenier.fr.schedulers.ProbeSchedulerFactory
import tmenier.fr.schedulers.ProbeSchedulerInterfaceType
import java.time.LocalDateTime

@ApplicationScoped
class ProbeWorkerService(
    private val probeSchedulerFactory: ProbeSchedulerFactory,
    private val probeRepository: ProbeRepository,
    private val probeAttemptRecorder: ProbeAttemptRecorder,
) {
    fun execute(
        task: ProbeCheckTaskDto,
        workerId: String,
    ) {
        val probeEntity = probeRepository.findById(task.probeId)
        if (!probeEntity.enabled) {
            logger.info { "Discarding Probe Check task ${task.id}: probe ${task.probeId} is disabled" }
            probeAttemptRecorder.discardDisabledTask(task.id, workerId)
            return
        }

        val probe = ProbeMapper.toDto(probeEntity)
        val handler =
            probeSchedulerFactory.getProtocol(probe.protocol)
                ?: throw IllegalArgumentException("Unknown Probe protocol ${probe.protocol}")

        @Suppress("UNCHECKED_CAST")
        val typedHandler = handler as ProbeSchedulerInterfaceType<Any>
        val isLastAttempt =
            probe.status == ProbeMonitorLogStatus.FAILURE ||
                task.attemptNumber >= probe.retry + 1

        logger.info {
            "Executing ${probe.protocol} Probe Check task ${task.id}: probe=${probe.id}, " +
                "attempt=${task.attemptNumber}/${probe.retry + 1}, lastAttempt=$isLastAttempt"
        }
        val result = typedHandler.execute(probe, probe.content, isLastAttempt)
        logger.info {
            "Probe Check task ${task.id} produced status=${result.status}, " +
                "responseTime=${result.responseTime}ms"
        }
        probeAttemptRecorder.completeAttempt(
            taskId = task.id,
            workerId = workerId,
            result = result,
            completedAt = LocalDateTime.now(),
        )
    }
}
