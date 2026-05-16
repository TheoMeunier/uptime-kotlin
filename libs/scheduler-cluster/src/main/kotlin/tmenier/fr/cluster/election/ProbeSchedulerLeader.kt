package tmenier.fr.cluster.election

import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.control.ActivateRequestContext
import jakarta.transaction.Transactional
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.reactive.messaging.Channel
import org.eclipse.microprofile.reactive.messaging.Emitter
import tmenier.fr.common.dtos.ProbeJob
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.repositories.ProbeRepository
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@ApplicationScoped
class ProbeSchedulerLeader(
    private val probeRepository: ProbeRepository,
    private val leaderElection: LeaderElection,
    @Channel("probe-jobs-out")
    private val probeEmitter: Emitter<ProbeJob>,
) {
    @ConfigProperty(name = "scheduler.strategy", defaultValue = "none")
    private lateinit var strategy: String

    @Scheduled(every = "1s", delay = 5, delayUnit = TimeUnit.SECONDS, concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    @ActivateRequestContext
    @Transactional
    fun publishDueProbes() {
        if (strategy != "rabbitmq") return
        if (!leaderElection.isLeader()) return

        logger.info { "Publishing due probes" }

        val now = LocalDateTime.now()
        val probes = probeRepository.findDueProbes(now)

        logger.info { "Found ${probes.size} due probes at $now" }

        probes.forEach {
            val job = ProbeJob(
                probeId = it.id,
                scheduledAt = now,
            )

            probeEmitter.send(job)
                .whenComplete { _, err ->
                    if (err != null) {
                        logger.error(err) { "Failed to publish probe job ${it.id}" }
                    } else {
                        logger.info { "Published probe job ${it.id} scheduled at $now" }
                    }
                }

            probeRepository.updateNextCheckAt(
                probeId = it.id,
                nextCheckAt = now.plusSeconds(it.interval.toLong())
            )
        }
    }
}

