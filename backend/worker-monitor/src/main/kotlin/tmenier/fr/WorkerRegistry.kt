package tmenier.fr

import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import org.eclipse.microprofile.config.inject.ConfigProperty
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.entities.WorkerHeartbeat
import tmenier.fr.databases.repositories.WorkerHeartbeatRepository
import java.net.InetAddress
import java.time.Instant

@ApplicationScoped
class WorkerRegistry(
    private val workerHeartbeatRepository: WorkerHeartbeatRepository
) {

    @ConfigProperty(name = "scheduler.worker.name", defaultValue = "true")
    private lateinit var myRegion: String
    
    @Scheduled(every = "10s")
    @Transactional
    fun heartbeat() {
        try {
            val workerId = "${myRegion}-${InetAddress.getLocalHost().hostName}"
            val existing = workerHeartbeatRepository.findById(myRegion)

            if (existing != null) {
                existing.workerId = workerId
                existing.lastSeenAt = Instant.now()
            } else {
                val heartbeat = WorkerHeartbeat()
                heartbeat.region = myRegion
                heartbeat.workerId = workerId
                heartbeat.lastSeenAt = Instant.now()
                workerHeartbeatRepository.persist(heartbeat)
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to send heartbeat for region $myRegion" }
        }
    }

    fun activeWorkerCount(): Int {
        return workerHeartbeatRepository.activeWorkerCount()
    }

}
