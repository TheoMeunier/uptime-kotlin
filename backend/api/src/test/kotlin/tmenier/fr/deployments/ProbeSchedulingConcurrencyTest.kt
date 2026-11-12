package tmenier.fr.deployments

import io.quarkus.narayana.jta.QuarkusTransaction
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tmenier.fr.databases.repositories.AdvisoryLockRepository
import tmenier.fr.databases.repositories.ProbeCheckTaskRepository
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.SECONDS

@QuarkusTest
@TestProfile(ApiOnlyProfile::class)
class ProbeSchedulingConcurrencyTest {
    @Inject
    lateinit var fixtures: QueueFixtures

    @Inject
    lateinit var repository: ProbeCheckTaskRepository

    @Inject
    lateinit var advisoryLockRepository: AdvisoryLockRepository

    private val probes = mutableListOf<UUID>()

    @AfterEach
    fun cleanUp() =
        probes.forEach {
            fixtures.disable(it)
            repository.cancelPending(it)
        }

    @Test
    fun `concurrent workers enqueue exactly one active job per Probe`() {
        repeat(PROBES) { probes += fixtures.createTcpProbe(REGION, port = 1, intervalSeconds = 3_600) }

        val pool = Executors.newFixedThreadPool(WORKERS)
        try {
            val start = CountDownLatch(1)
            val runs =
                (1..WORKERS).map {
                    pool.submit<Int> {
                        start.await()
                        (1..ROUNDS).sumOf { repository.ensureScheduledProbeChecks(REGION, limit = 500) }
                    }
                }
            start.countDown()
            runs.forEach { it.get(60, SECONDS) }
        } finally {
            pool.shutdownNow()
        }

        probes.forEach { probeId ->
            val active = fixtures.queuedChecks(probeId).filter { it.status in ACTIVE }
            assertEquals(1, active.size, "Probe $probeId must have exactly one active job: $active")
        }
    }

    @Test
    fun `a worker does not schedule while another one holds the scheduler lock`() {
        val probeId = fixtures.createTcpProbe(REGION, port = 1, intervalSeconds = 3_600).also(probes::add)

        val locked = CountDownLatch(1)
        val release = CountDownLatch(1)
        val holder = Executors.newSingleThreadExecutor()
        try {
            val held =
                holder.submit {
                    QuarkusTransaction.requiringNew().run {
                        check(advisoryLockRepository.tryLock(AdvisoryLockRepository.PROBE_SCHEDULER))
                        locked.countDown()
                        release.await(30, SECONDS)
                    }
                }
            assertTrue(locked.await(10, SECONDS), "the other worker never took the lock")

            assertEquals(0, repository.ensureScheduledProbeChecks(REGION, limit = 500))
            assertEquals(emptyList<QueuedCheck>(), fixtures.queuedChecks(probeId))

            release.countDown()
            held.get(10, SECONDS)

            assertTrue(repository.ensureScheduledProbeChecks(REGION, limit = 500) >= 1)
            assertEquals(1, fixtures.queuedChecks(probeId).count { it.status in ACTIVE })
        } finally {
            release.countDown()
            holder.shutdownNow()
        }
    }

    companion object {
        private const val REGION = ApiOnlyProfile.REGION
        private const val WORKERS = 3
        private const val ROUNDS = 10
        private const val PROBES = 20
        private val ACTIVE = setOf("PENDING", "LEASED")
    }
}
