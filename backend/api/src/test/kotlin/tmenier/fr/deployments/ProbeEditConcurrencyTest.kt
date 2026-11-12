package tmenier.fr.deployments

import io.quarkus.narayana.jta.QuarkusTransaction
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.databases.dtos.StoreProbeDto
import tmenier.fr.databases.entities.ProbesEntity
import tmenier.fr.databases.mappers.ProbeMapper
import tmenier.fr.databases.repositories.ProbeRepository
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.SECONDS

@QuarkusTest
@TestProfile(ApiOnlyProfile::class)
class ProbeEditConcurrencyTest {
    @Inject
    lateinit var fixtures: QueueFixtures

    @Inject
    lateinit var probeRepository: ProbeRepository

    @Inject
    lateinit var em: EntityManager

    private val probes = mutableListOf<UUID>()

    @AfterEach
    fun cleanUp() = probes.forEach(fixtures::disable)

    @Test
    fun `editing a Probe keeps the status written concurrently by a worker`() {
        val probeId =
            fixtures
                .createTcpProbe(
                    ApiOnlyProfile.REGION,
                    port = 1,
                    intervalSeconds = 3_600,
                    currentStatus = ProbeMonitorLogStatus.SUCCESS,
                ).also(probes::add)
        val rename = renameDto(probeId)

        val workerHoldsLock = CountDownLatch(1)
        val workerMayCommit = CountDownLatch(1)
        val pool = Executors.newFixedThreadPool(2)
        try {
            val worker =
                pool.submit {
                    QuarkusTransaction.requiringNew().run {
                        probeRepository.findByIdForUpdate(probeId).status = ProbeMonitorLogStatus.FAILURE
                        em.flush()
                        workerHoldsLock.countDown()
                        workerMayCommit.await(30, SECONDS)
                    }
                }
            assertTrue(workerHoldsLock.await(10, SECONDS), "the worker never locked the Probe")

            val edit =
                pool.submit {
                    QuarkusTransaction.requiringNew().run {
                        probeRepository.update(rename, emptyList())
                    }
                }
            awaitSessionBlockedOnLock()

            workerMayCommit.countDown()
            worker.get(10, SECONDS)
            edit.get(10, SECONDS)

            val (name, status) = nameAndStatus(probeId)
            assertEquals("renamed-$probeId", name)
            assertEquals(ProbeMonitorLogStatus.FAILURE, status, "the edit overwrote the worker's status")
        } finally {
            workerMayCommit.countDown()
            pool.shutdownNow()
        }
    }

    private fun renameDto(probeId: UUID): StoreProbeDto =
        QuarkusTransaction.requiringNew().call {
            val probe = ProbeMapper.toDto(probeRepository.findById(probeId))
            StoreProbeDto(
                id = probe.id,
                name = "renamed-$probeId",
                interval = probe.interval,
                timeout = probe.timeout,
                intervalRetry = probe.intervalRetry,
                retry = probe.retry,
                protocol = probe.protocol,
                enabled = probe.enabled,
                description = probe.description,
                content = probe.content,
                alertRepeatSeconds = probe.alertRepeatSeconds,
                logRetentionDays = probe.logRetentionDays,
            )
        }

    private fun nameAndStatus(probeId: UUID): Pair<String, ProbeMonitorLogStatus> =
        QuarkusTransaction.requiringNew().call {
            val probe = em.find(ProbesEntity::class.java, probeId)
            probe.name to probe.status
        }

    private fun awaitSessionBlockedOnLock() {
        val deadline = System.nanoTime() + 10_000_000_000L
        while (System.nanoTime() < deadline) {
            val waiting =
                QuarkusTransaction.requiringNew().call {
                    (
                        em
                            .createNativeQuery(
                                """
                            SELECT count(*) FROM pg_stat_activity
                            WHERE datname = current_database() AND wait_event_type = 'Lock'
                            """,
                            ).singleResult as Number
                    ).toInt()
                }
            if (waiting > 0) return
            Thread.sleep(20)
        }
        fail<Unit>("the edit never blocked on the Probe row lock")
    }
}
