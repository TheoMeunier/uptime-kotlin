package tmenier.fr.monitors

import jakarta.enterprise.context.ApplicationScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.dtos.ProbeCheckTaskDto
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.databases.repositories.ProbeCheckTaskRepository
import tmenier.fr.notifications.services.NotificationService
import tmenier.fr.schedulers.ProbeSchedulerInterfaceType
import tmenier.fr.schedulers.services.SaveProbeMonitor
import kotlin.time.Duration.Companion.milliseconds

@ApplicationScoped
class ProbeWorkerService(
    private val probeCheckTaskRepository: ProbeCheckTaskRepository,
    private val saveProbeMonitorLog: SaveProbeMonitor,
    private val notificationService: NotificationService,
) {

    /**
     * Some worker activity, execute a single attempt of the probe check task, if it fails and is not the last attempt, it will cascade to other regions
     */
    suspend fun executeSingleAttemptWithCascade(
        probeCheckTask: ProbeCheckTaskDto,
        probe: ProbeDTO,
        typeHandler: ProbeSchedulerInterfaceType<Any>,
        region: String
    ) {
        val isLastAttempt = probeCheckTask.attemptNumber >= probe.retry + 1
        val result = typeHandler.execute(probe, probe.content, isLastAttempt)

        logger.info { "Probe ${probe.id} executed with status ${result.status} (region=$region, tentative ${probeCheckTask.attemptNumber})" }

        when (result.status) {
            ProbeMonitorLogStatus.SUCCESS -> {
                logger.info { "Probe ${probe.id} ${result.status} (region=$region, tentative ${probeCheckTask.attemptNumber})" }
                saveAndPublishNotification(probeCheckTask, result, probe.status)

                withContext(Dispatchers.IO) {
                    probeCheckTaskRepository.markSuccess(probeCheckTask, result.message)
                }
            }

            ProbeMonitorLogStatus.WARNING, ProbeMonitorLogStatus.FAILURE -> {
                logger.warn { "Probe ${probe.id} ${result.status} tentative ${probeCheckTask.attemptNumber} (region=$region)" }

                if (isLastAttempt) {
                    saveAndPublishNotification(
                        probeCheckTask,
                        result.copy(status = ProbeMonitorLogStatus.FAILURE),
                        probe.status,
                    )
                } else {
                    logger.info { "Probe ${probe.id} will retry in ${probe.interval} seconds (region=$region)" }
                }

                withContext(Dispatchers.IO) {
                    probeCheckTaskRepository.markFailedAndMaybeCascade(probeCheckTask, result.message, probe)
                    saveProbeMonitorLog.saveProbeMonitorLog(probe.id, probeCheckTask.scheduledAt, result)
                }
            }

            else -> logger.warn { "Probe ${probe.id} ${result.status}" }
        }
    }

    /**
     * one worker for check task, local repeat loop, no cascade to other regions
     */
    suspend fun executeLocalRetryLoop(
        probeCheckTask: ProbeCheckTaskDto,
        probe: ProbeDTO,
        typeHandler: ProbeSchedulerInterfaceType<Any>,
    ) {
        val maxAttempts = if (probe.status == ProbeMonitorLogStatus.FAILURE) 1 else probe.retry + 1

        repeat(maxAttempts) { attempt ->
            val isLastAttempt = attempt == maxAttempts - 1
            val result = typeHandler.execute(probe, probe.content, isLastAttempt)

            when (result.status) {
                ProbeMonitorLogStatus.SUCCESS -> {
                    logger.info { "Probe ${probe.id} ${result.status} after ${attempt + 1} attempt(s)" }
                    saveAndPublishNotification(probeCheckTask, result, probe.status)

                    withContext(Dispatchers.IO) {
                        probeCheckTaskRepository.markSuccess(probeCheckTask, result.message)
                    }

                    return
                }

                ProbeMonitorLogStatus.WARNING, ProbeMonitorLogStatus.FAILURE -> {
                    logger.warn { "Probe ${probe.id} ${result.status} on attempt ${attempt + 1}/$maxAttempts" }

                    if (isLastAttempt) {
                        saveAndPublishNotification(
                            probeCheckTask,
                            result.copy(status = ProbeMonitorLogStatus.FAILURE),
                            probe.status,
                        )
                        return
                    }

                    withContext(Dispatchers.IO) {
                        saveProbeMonitorLog.saveProbeMonitorLog(probe.id, probeCheckTask.scheduledAt, result)
                    }

                    delay((probe.interval * 1000L).milliseconds)
                }

                else -> logger.warn { "Probe ${probe.id} ${result.status}" }
            }
        }
    }

    private suspend fun saveAndPublishNotification(
        probeCheckTask: ProbeCheckTaskDto,
        result: ProbeResult,
        previousStatus: ProbeMonitorLogStatus,
    ) {
        try {
            withContext(Dispatchers.IO) {
                saveProbeMonitorLog.saveProbeMonitorLog(probeCheckTask.probeId, probeCheckTask.scheduledAt, result)
            }

            notificationService.sendNotification(
                probeId = probeCheckTask.probeId,
                result = result,
                previousStatus = previousStatus
            )

        } catch (e: Exception) {
            logger.error(e) { "Failed to save probe monitor log" }
            throw e
        }
    }
}
