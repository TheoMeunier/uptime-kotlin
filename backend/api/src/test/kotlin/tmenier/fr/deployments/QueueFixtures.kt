package tmenier.fr.deployments

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.databases.dtos.StoreProbeCheckTaskDto
import tmenier.fr.databases.entities.ProbesEntity
import tmenier.fr.databases.repositories.ProbeCheckTaskRepository
import tmenier.fr.databases.repositories.WorkerHeartbeatRepository
import java.time.Instant
import java.util.UUID

data class ProbeRun(
    val runAt: Instant,
    val jobId: UUID?,
)

data class QueuedCheck(
    val attempt: Int,
    val status: String,
    val availableAt: Instant,
)

/** Direct database access for the deployment tests: create Probes, read what the engine did. */
@ApplicationScoped
class QueueFixtures(
    private val em: EntityManager,
    private val objectMapper: ObjectMapper,
    private val probeCheckTaskRepository: ProbeCheckTaskRepository,
    private val workerHeartbeatRepository: WorkerHeartbeatRepository,
) {
    @Transactional
    fun createTcpProbe(
        region: String,
        port: Int,
        intervalSeconds: Int,
        retry: Int = 0,
        intervalRetrySeconds: Int = 60,
        currentStatus: ProbeMonitorLogStatus = ProbeMonitorLogStatus.SUCCESS,
    ): UUID {
        val probe =
            ProbesEntity().apply {
                id = UUID.randomUUID()
                name = "it-$region-$port"
                interval = intervalSeconds
                timeout = 5
                this.retry = retry
                intervalRetry = intervalRetrySeconds
                enabled = true
                status = currentStatus
                protocol = ProbeProtocol.TCP
                content = objectMapper.valueToTree(mapOf("url" to "127.0.0.1", "tcpPort" to port))
                regionsOrder = objectMapper.valueToTree(listOf(region))
                nextCheckAt = Instant.now()
            }
        em.persist(probe)
        return probe.id
    }

    @Transactional
    fun createTcpProbeQueuedIn(
        jobRegion: String,
        probeRegion: String,
        port: Int,
    ): Pair<UUID, UUID> {
        val probeId = createTcpProbe(probeRegion, port, intervalSeconds = 3_600)
        val job =
            probeCheckTaskRepository.store(
                StoreProbeCheckTaskDto(
                    probeId = probeId,
                    region = jobRegion,
                    attemptNumber = 1,
                    scheduleAt = Instant.now(),
                    availableAt = Instant.now(),
                ),
            )
        return probeId to job.id
    }

    fun beat(region: String) = workerHeartbeatRepository.beat("it-$region", region)

    @Transactional
    fun disable(probeId: UUID) {
        em
            .createNativeQuery("UPDATE probes SET enabled = false WHERE id = :id")
            .setParameter("id", probeId)
            .executeUpdate()
    }

    @Transactional
    fun runs(probeId: UUID): List<ProbeRun> =
        rows(
            "SELECT run_at, probe_check_job_id FROM probes_monitors_logs WHERE probe_id = :id ORDER BY run_at",
            probeId,
        ).map { ProbeRun(toInstant(it[0]), it[1] as UUID?) }

    @Transactional
    fun queuedChecks(probeId: UUID): List<QueuedCheck> =
        rows(
            "SELECT probe_attempt, status, available_at FROM probe_check_jobs WHERE probe_id = :id ORDER BY probe_attempt",
            probeId,
        ).map { QueuedCheck((it[0] as Number).toInt(), it[1].toString(), toInstant(it[2])) }

    @Transactional
    fun heartbeatsInRegion(
        region: String,
        since: Instant,
    ): Long =
        (
            em
                .createNativeQuery("SELECT count(*) FROM worker_heartbeats WHERE region = :region AND last_seen_at >= :since")
                .setParameter("region", region)
                .setParameter("since", since)
                .singleResult as Number
        ).toLong()

    @Suppress("UNCHECKED_CAST")
    private fun rows(
        sql: String,
        probeId: UUID,
    ): List<Array<Any?>> =
        em
            .createNativeQuery(sql)
            .setParameter("id", probeId)
            .resultList as List<Array<Any?>>

    private fun toInstant(value: Any?): Instant =
        when (value) {
            is Instant -> value
            is java.time.OffsetDateTime -> value.toInstant()
            is java.sql.Timestamp -> value.toInstant()
            else -> error("Unexpected timestamp type ${value?.javaClass}")
        }
}
