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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.databases.entities.ProbesEntity
import tmenier.fr.databases.entities.ProbesMonitorsLogEntity
import tmenier.fr.databases.repositories.InstanceSettingsRepository
import tmenier.fr.schedulers.jobs.ProbeLogRetentionService
import java.time.LocalDateTime
import java.util.UUID

/**
 * Runs against the API-only profile: no background job competes with the purge
 * triggered here by hand.
 */
@QuarkusTest
@TestProfile(ApiOnlyProfile::class)
class ProbeLogRetentionTest {
    @Inject
    lateinit var fixtures: RetentionFixtures

    @Inject
    lateinit var retention: ProbeLogRetentionService

    private val now = LocalDateTime.now()
    private val ages = listOf(1L, 29L, 45L, 100L, 400L)
    private val probes = mutableListOf<UUID>()

    @BeforeEach
    fun resetDefault() = fixtures.setDefault(null)

    @AfterEach
    fun cleanUp() {
        probes.forEach(fixtures::delete)
        fixtures.setDefault(null)
    }

    @Test
    fun `nothing is purged while neither the instance nor the probe set a retention`() {
        val probe = fixtures.probeWithLogs(override = null, ageInDays = ages, now = now).also(probes::add)

        retention.purge(now)

        assertEquals(ages.size.toLong(), fixtures.logCount(probe))
    }

    @Test
    fun `a probe without override follows the instance default`() {
        fixtures.setDefault(90)
        val probe = fixtures.probeWithLogs(override = null, ageInDays = ages, now = now).also(probes::add)

        retention.purge(now)

        assertEquals(3L, fixtures.logCount(probe), "1, 29 and 45 days are kept; 100 and 400 go")
    }

    @Test
    fun `a probe override wins over the instance default, in both directions`() {
        fixtures.setDefault(90)
        val shorter = fixtures.probeWithLogs(override = 30, ageInDays = ages, now = now).also(probes::add)
        val longer = fixtures.probeWithLogs(override = 365, ageInDays = ages, now = now).also(probes::add)
        val forever = fixtures.probeWithLogs(override = 0, ageInDays = ages, now = now).also(probes::add)

        retention.purge(now)

        assertEquals(2L, fixtures.logCount(shorter))
        assertEquals(4L, fixtures.logCount(longer))
        assertEquals(ages.size.toLong(), fixtures.logCount(forever))
    }

    @Test
    fun `a probe override applies even when the instance keeps everything`() {
        val probe = fixtures.probeWithLogs(override = 30, ageInDays = ages, now = now).also(probes::add)

        retention.purge(now)

        assertEquals(2L, fixtures.logCount(probe))
    }

    @Test
    fun `the purge goes batch by batch and stops at the cap of a run`() {
        val probe = fixtures.probeWithLogs(override = 30, ageInDays = List(25) { 60L }, now = now).also(probes::add)

        val firstRun = retention.purge(now, batchSize = 10, maxPerRun = 15)
        assertEquals(15, firstRun)
        assertEquals(10L, fixtures.logCount(probe))

        val secondRun = retention.purge(now, batchSize = 10, maxPerRun = 15)
        assertEquals(10, secondRun)
        assertEquals(0L, fixtures.logCount(probe))
    }
}

@ApplicationScoped
class RetentionFixtures(
    private val em: EntityManager,
    private val objectMapper: ObjectMapper,
    private val instanceSettingsRepository: InstanceSettingsRepository,
) {
    @Transactional
    fun setDefault(days: Int?) {
        instanceSettingsRepository.updateLogRetentionDays(days)
    }

    @Transactional
    fun probeWithLogs(
        override: Int?,
        ageInDays: List<Long>,
        now: LocalDateTime,
    ): UUID {
        val probe =
            ProbesEntity().apply {
                id = UUID.randomUUID()
                name = "it-retention-${id.toString().take(8)}"
                interval = 3_600
                timeout = 5
                retry = 0
                intervalRetry = 60
                enabled = false
                protocol = ProbeProtocol.TCP
                content = objectMapper.valueToTree(mapOf("url" to "127.0.0.1", "tcpPort" to 1))
                logRetentionDays = override
            }
        em.persist(probe)

        ageInDays.forEach { days ->
            em.persist(
                ProbesMonitorsLogEntity().apply {
                    id = UUID.randomUUID()
                    status = ProbeMonitorLogStatus.SUCCESS
                    message = "it"
                    this.probe = probe
                    runAt = now.minusDays(days)
                },
            )
        }
        return probe.id
    }

    @Transactional
    fun logCount(probeId: UUID): Long =
        (
            em
                .createNativeQuery("SELECT count(*) FROM probes_monitors_logs WHERE probe_id = :id")
                .setParameter("id", probeId)
                .singleResult as Number
        ).toLong()

    @Transactional
    fun delete(probeId: UUID) {
        em.createNativeQuery("DELETE FROM probes_monitors_logs WHERE probe_id = :id").setParameter("id", probeId).executeUpdate()
        em.createNativeQuery("DELETE FROM probes WHERE id = :id").setParameter("id", probeId).executeUpdate()
    }
}
