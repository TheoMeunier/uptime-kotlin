package tmenier.fr.monitors

import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.repositories.WorkerHeartbeatRepository
import java.net.InetAddress

@ApplicationScoped
class WorkerRegistry(
    private val workerHeartbeatRepository: WorkerHeartbeatRepository,
) {
    @ConfigProperty(name = "scheduler.worker.name", defaultValue = "default")
    private lateinit var myRegion: String

    // Same identifier as the one used by ProbeWorker to claim tasks (claimed_by).
    private val workerId by lazy { "$myRegion-${InetAddress.getLocalHost().hostName}" }

    @Scheduled(every = "10s", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    fun heartbeat() {
        try {
            workerHeartbeatRepository.beat(workerId, myRegion)
        } catch (e: Exception) {
            logger.error(e) { "Failed to send heartbeat for worker $workerId (region $myRegion)" }
        }
    }

    @Scheduled(every = "10m", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    fun purgeStaleHeartbeats() {
        try {
            val purged = workerHeartbeatRepository.purgeStale()
            if (purged > 0) logger.info { "Purged $purged stale worker heartbeat(s)" }
        } catch (e: Exception) {
            logger.warn(e) { "Failed to purge stale worker heartbeats" }
        }
    }

    fun activeWorkerCount(): Int = workerHeartbeatRepository.activeWorkerCount()

    fun activeWorkerCountInRegion(): Int = workerHeartbeatRepository.activeWorkerCount(myRegion)
}
