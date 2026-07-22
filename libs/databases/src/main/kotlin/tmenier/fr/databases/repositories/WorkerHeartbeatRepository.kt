package tmenier.fr.databases.repositories

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import tmenier.fr.databases.entities.WorkerHeartbeat
import java.time.Instant

@ApplicationScoped
class WorkerHeartbeatRepository : PanacheRepositoryBase<WorkerHeartbeat, String> {

    override fun findById(id: String): WorkerHeartbeat? {
        return find("region = ?1", id).firstResult()
    }


    @Transactional
    fun activeWorkerCount(): Int {
        return count(
            "lastSeenAt > ?1",
            Instant.now().minusSeconds(30),
        ).toInt()
    }
}
