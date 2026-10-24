package tmenier.fr.deployments

import com.fasterxml.jackson.databind.ObjectMapper
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.databases.entities.ProbesEntity
import tmenier.fr.databases.repositories.ProbeLogRollupRepository
import tmenier.fr.schedulers.jobs.ProbeLogRollupService
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@QuarkusTest
@TestProfile(ApiOnlyProfile::class)
class ProbeLogRollupTest {
    @Inject
    lateinit var fixtures: RollupFixtures

    @Inject
    lateinit var job: ProbeLogRollupService

    @Inject
    lateinit var rollups: ProbeLogRollupRepository

    private val probes = mutableListOf<UUID>()

    /** 14:30 UTC: the last whole hour is 13:00-14:00. */
    private val now = Instant.parse("2026-10-20T14:30:00Z")
    private val tenOClock = Instant.parse("2026-10-20T10:00:00Z")

    @BeforeEach
    fun startFromAKnownWatermark() = fixtures.setWatermark(Instant.parse("2026-10-20T09:00:00Z"))

    @AfterEach
    fun cleanUp() {
        probes.forEach(fixtures::deleteProbe)
        // Other tests read uptimes at fixed instants: without a watermark every read stays on the raw table.
        fixtures.setWatermark(null)
    }

    @Test
    fun `an hour is summarised into counters, maintenance kept apart`() {
        val probe = fixtures.probe().also(probes::add)
        fixtures.log(probe, tenOClock.plusSeconds(60), SUCCESS, responseTime = 100)
        fixtures.log(probe, tenOClock.plusSeconds(120), SUCCESS, responseTime = 300)
        fixtures.log(probe, tenOClock.plusSeconds(180), WARNING, responseTime = 900)
        fixtures.log(probe, tenOClock.plusSeconds(240), FAILURE, responseTime = 5_000)
        fixtures.log(probe, tenOClock.plusSeconds(300), PAUSE, responseTime = 0)
        fixtures.log(probe, tenOClock.plusSeconds(360), SUCCESS, responseTime = 50, underMaintenance = true)

        job.rollUp(now)

        val hour = fixtures.hourly(probe, tenOClock)
        assertNotNull(hour, "the hour is summarised")
        assertEquals(5L, hour!!.total, "PAUSE counts, as in the raw queries; maintenance does not")
        assertEquals(2L, hour.successes)
        assertEquals(1L, hour.warnings)
        assertEquals(1L, hour.failures)
        assertEquals(1L, hour.maintenance)
        assertEquals(100L + 300 + 900 + 50, hour.latencySum, "latency of SUCCESS and WARNING only: the timeout stays out")
        assertEquals(4L, hour.latencyCount)
        assertEquals(tenOClock.plusSeconds(120), hour.lastSuccessAt, "last success outside maintenance")
        assertEquals(Instant.parse("2026-10-20T14:00:00Z"), rollups.watermark(), "up to the last whole hour")
    }

    @Test
    fun `the hour in progress is left to the raw table`() {
        val probe = fixtures.probe().also(probes::add)
        fixtures.log(probe, Instant.parse("2026-10-20T14:10:00Z"), SUCCESS)

        job.rollUp(now)

        assertEquals(null, fixtures.hourly(probe, Instant.parse("2026-10-20T14:00:00Z")))
    }

    @Test
    fun `running again recomputes the last hours without counting twice, and picks up a late check`() {
        val probe = fixtures.probe().also(probes::add)
        val hour = Instant.parse("2026-10-20T12:00:00Z")
        fixtures.log(probe, hour.plusSeconds(60), SUCCESS)

        job.rollUp(now)
        job.rollUp(now)
        assertEquals(1L, fixtures.hourly(probe, hour)!!.total)

        fixtures.log(probe, hour.plusSeconds(120), FAILURE)
        job.rollUp(now.plus(1, ChronoUnit.HOURS))

        val recomputed = fixtures.hourly(probe, hour)!!
        assertEquals(2L, recomputed.total)
        assertEquals(1L, recomputed.failures)
    }

    @Test
    fun `uptimes keep their history once the raw checks are purged`() {
        val probe = fixtures.probe().also(probes::add)
        repeat(9) { fixtures.log(probe, tenOClock.plusSeconds(60L * (it + 1)), SUCCESS) }
        fixtures.log(probe, tenOClock.plusSeconds(600), FAILURE)
        // After the watermark: only in the raw table.
        fixtures.log(probe, Instant.parse("2026-10-20T14:20:00Z"), FAILURE)

        job.rollUp(now)
        val before = rollups.uptimeCounts(listOf(probe), tenOClock.minusSeconds(3_600), now)[probe]!!

        fixtures.deleteRawBefore(probe, Instant.parse("2026-10-20T14:00:00Z"))
        val after = rollups.uptimeCounts(listOf(probe), tenOClock.minusSeconds(3_600), now)[probe]!!

        assertEquals(11L, before.total)
        assertEquals(9L, before.successes)
        assertEquals(before, after, "same figures from the rollups as from the raw checks")
    }

    @Test
    fun `without any watermark every read stays on the raw table`() {
        val probe = fixtures.probe().also(probes::add)
        fixtures.log(probe, tenOClock.plusSeconds(60), SUCCESS)
        fixtures.log(probe, tenOClock.plusSeconds(120), FAILURE)

        val counts = rollups.uptimeCounts(listOf(probe), tenOClock, now, watermark = null)[probe]!!

        assertEquals(2L, counts.total)
        assertEquals(50.0, counts.percent, 0.001)
    }

    @Test
    fun `the first run backfills a day per chunk, up to the cap of a run`() {
        fixtures.setWatermark(null)
        val probe = fixtures.probe().also(probes::add)
        fixtures.log(probe, Instant.parse("2020-01-01T10:00:00Z"), SUCCESS)
        val oldest = rollups.oldestRawRunAt()!!.truncatedTo(ChronoUnit.HOURS)

        assertEquals(2, job.rollUp(now, maxDays = 2))
        assertEquals(oldest.plus(2, ChronoUnit.DAYS), rollups.watermark(), "the watermark moves chunk by chunk")
    }

    companion object {
        private val SUCCESS = ProbeMonitorLogStatus.SUCCESS.ordinal
        private val WARNING = ProbeMonitorLogStatus.WARNING.ordinal
        private val PAUSE = ProbeMonitorLogStatus.PAUSE.ordinal
        private val FAILURE = ProbeMonitorLogStatus.FAILURE.ordinal
    }
}

data class HourlyRow(
    val total: Long,
    val successes: Long,
    val warnings: Long,
    val failures: Long,
    val maintenance: Long,
    val latencySum: Long,
    val latencyCount: Long,
    val lastSuccessAt: Instant?,
)

@ApplicationScoped
class RollupFixtures(
    private val em: EntityManager,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun setWatermark(until: Instant?) {
        if (until == null) {
            em.createNativeQuery("UPDATE probe_log_rollup_state SET rolled_up_until = NULL WHERE id = 1").executeUpdate()
            return
        }
        em
            .createNativeQuery("UPDATE probe_log_rollup_state SET rolled_up_until = CAST(:until AS timestamptz) WHERE id = 1")
            .setParameter("until", until)
            .executeUpdate()
    }

    @Transactional
    fun probe(): UUID {
        val probe =
            ProbesEntity().apply {
                id = UUID.randomUUID()
                name = "it-rollup-${id.toString().take(8)}"
                interval = 3_600
                timeout = 5
                retry = 0
                intervalRetry = 60
                enabled = false
                protocol = ProbeProtocol.TCP
                content = objectMapper.valueToTree(mapOf("url" to "127.0.0.1", "tcpPort" to 1))
            }
        em.persist(probe)
        return probe.id
    }

    @Transactional
    fun log(
        probeId: UUID,
        at: Instant,
        status: Int,
        responseTime: Int = 10,
        underMaintenance: Boolean = false,
    ) {
        em
            .createNativeQuery(
                """
                INSERT INTO probes_monitors_logs (id, probe_id, status, response_time, message, run_at, under_maintenance)
                VALUES (:id, :probe, :status, :responseTime, 'test', :at, :maintenance)
                """.trimIndent(),
            ).setParameter("id", UUID.randomUUID())
            .setParameter("probe", probeId)
            .setParameter("status", status)
            .setParameter("responseTime", responseTime)
            .setParameter("at", at)
            .setParameter("maintenance", underMaintenance)
            .executeUpdate()
    }

    @Transactional
    fun hourly(
        probeId: UUID,
        bucketStart: Instant,
    ): HourlyRow? {
        @Suppress("UNCHECKED_CAST")
        val row =
            em
                .createNativeQuery(
                    """
                    SELECT checks_total, checks_success, checks_warning, checks_failure, checks_maintenance,
                           response_time_sum, response_time_count, last_success_at
                    FROM probe_logs_hourly
                    WHERE probe_id = :probe AND bucket_start = CAST(:start AS timestamptz)
                    """.trimIndent(),
                ).setParameter("probe", probeId)
                .setParameter("start", bucketStart)
                .resultList
                .firstOrNull() as Array<Any?>? ?: return null

        return HourlyRow(
            total = (row[0] as Number).toLong(),
            successes = (row[1] as Number).toLong(),
            warnings = (row[2] as Number).toLong(),
            failures = (row[3] as Number).toLong(),
            maintenance = (row[4] as Number).toLong(),
            latencySum = (row[5] as Number).toLong(),
            latencyCount = (row[6] as Number).toLong(),
            lastSuccessAt =
                when (val at = row[7]) {
                    null -> null
                    is Instant -> at
                    is java.time.OffsetDateTime -> at.toInstant()
                    is java.sql.Timestamp -> at.toInstant()
                    else -> error("Unexpected timestamp type ${at.javaClass}")
                },
        )
    }

    @Transactional
    fun deleteRawBefore(
        probeId: UUID,
        before: Instant,
    ) {
        em
            .createNativeQuery("DELETE FROM probes_monitors_logs WHERE probe_id = :probe AND run_at < CAST(:before AS timestamptz)")
            .setParameter("probe", probeId)
            .setParameter("before", before)
            .executeUpdate()
    }

    @Transactional
    fun deleteProbe(probeId: UUID) {
        em.createNativeQuery("DELETE FROM probes WHERE id = :id").setParameter("id", probeId).executeUpdate()
    }
}
