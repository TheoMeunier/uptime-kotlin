package tmenier.fr.cluster.election

import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.control.ActivateRequestContext
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.reactive.messaging.Channel
import org.eclipse.microprofile.reactive.messaging.Emitter
import tmenier.fr.common.dtos.ProbeJob
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.mappers.ProbeMapper
import tmenier.fr.databases.repositories.ProbeRepository
import java.time.LocalDateTime

@ApplicationScoped
class ProbeSchedulerLeader(
    private val probeRepository: ProbeRepository,
    private val leaderElection: LeaderElection,
    @Channel("probe-jobs-out")
    private val probeEmitter: Emitter<ProbeJob>,
) {
    @ConfigProperty(name = "probe.scheduler.strategy", defaultValue = "none")
    private lateinit var strategy: String

    @Scheduled(every = "1s", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    @ActivateRequestContext
    fun publishDueProbes() {
        if (strategy != "rabbitmq") return
        if (!leaderElection.isLeader()) return

        val now = LocalDateTime.now()

        probeRepository.findDueProbes(now).forEach {
            val probe = ProbeMapper.toDto(it)

            val job = ProbeJob(
                probeId = probe.id,
                scheduledAt = now,
                protocol = probe.protocol,
                interval = probe.interval,
                retry = probe.retry,
                content = probe.content,
            )

            probeEmitter.send(job)
                .whenComplete { _, err ->
                    if (err != null) {
                        logger.error(err) { "Failed to publish probe job ${probe.id}" }
                    } else {
                        logger.debug { "Published probe job ${probe.id} scheduled at $now" }
                    }
                }

            probeRepository.updateNextCheckAt(
                probeId = probe.id,
                nextCheckAt = now.plusSeconds(probe.interval.toLong())
            )
        }
    }
}

