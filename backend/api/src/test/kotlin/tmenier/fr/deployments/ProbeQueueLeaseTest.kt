package tmenier.fr.deployments

import io.quarkus.narayana.jta.QuarkusTransaction
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import tmenier.fr.common.enums.probes.ImmediateCheckOutcome
import tmenier.fr.databases.repositories.ProbeCheckTaskRepository
import java.time.Duration
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.SECONDS

@QuarkusTest
@TestProfile(ApiOnlyProfile::class)
class ProbeQueueLeaseTest {
    @Inject
    lateinit var fixtures: QueueFixtures

    @Inject
    lateinit var repository: ProbeCheckTaskRepository

    @Inject
    lateinit var em: EntityManager

    private val probes = mutableListOf<UUID>()

    @AfterEach
    fun cleanUp() =
        probes.forEach { probeId ->
            fixtures.disable(probeId)
            QuarkusTransaction.requiringNew().run {
                em.createNativeQuery("DELETE FROM probe_check_jobs WHERE probe_id = :id").setParameter("id", probeId).executeUpdate()
            }
        }

    @Test
    fun `check now does not undo a claim that lands while it runs`() {
        val jobId = queuedJob()

        val claimerHoldsLock = CountDownLatch(1)
        val claimerMayCommit = CountDownLatch(1)
        val pool = Executors.newFixedThreadPool(2)
        try {
            val claimer =
                pool.submit {
                    QuarkusTransaction.requiringNew().run {
                        lease(jobId, owner = CLAIMER)
                        claimerHoldsLock.countDown()
                        claimerMayCommit.await(30, SECONDS)
                    }
                }
            assertTrue(claimerHoldsLock.await(10, SECONDS), "the claimer never locked the job")

            val checkNow = pool.submit<ImmediateCheckOutcome> { repository.requestImmediateCheck(probeOf(jobId)) }
            awaitSessionBlockedOnLock()

            claimerMayCommit.countDown()
            claimer.get(10, SECONDS)
            val outcome = checkNow.get(10, SECONDS)

            val job = job(jobId)
            assertEquals("LEASED", job.status, "check now put the claimed job back to PENDING")
            assertEquals(CLAIMER, job.leaseOwner)
            assertEquals(ImmediateCheckOutcome.ALREADY_RUNNING, outcome)
        } finally {
            claimerMayCommit.countDown()
            pool.shutdownNow()
        }
    }

    @Test
    fun `renewing leases uses the database clock and only touches the worker's own tasks`() {
        val mine = queuedJob()
        val other = queuedJob()
        QuarkusTransaction.requiringNew().run {
            lease(mine, owner = CLAIMER, leaseSeconds = 5)
            lease(other, owner = OTHER_WORKER, leaseSeconds = 5)
        }

        repository.renewLeases(CLAIMER, Duration.ofMinutes(2), listOf(mine, other))

        assertTrue(leaseEndsAfter(mine, seconds = 100), "the lease was not renewed from the database clock")
        assertTrue(!leaseEndsAfter(other, seconds = 100), "another worker's lease was renewed")
    }

    @Test
    fun `a stopping worker puts its unfinished tasks back without spending a delivery attempt`() {
        val mine = queuedJob()
        val other = queuedJob()
        QuarkusTransaction.requiringNew().run {
            lease(mine, owner = CLAIMER)
            lease(other, owner = OTHER_WORKER)
        }
        val attemptsBefore = job(mine).deliveryAttempts

        val released = repository.releaseLeases(CLAIMER, listOf(mine, other))

        assertEquals(1, released)
        val job = job(mine)
        assertEquals("PENDING", job.status)
        assertNull(job.leaseOwner)
        assertEquals(attemptsBefore, job.deliveryAttempts)
        assertEquals("LEASED", job(other).status, "another worker's task was released")
    }

    private data class JobRow(
        val status: String,
        val leaseOwner: String?,
        val deliveryAttempts: Int,
    )

    private fun queuedJob(): UUID {
        val (probeId, jobId) = fixtures.createTcpProbeQueuedIn(ApiOnlyProfile.REGION, ApiOnlyProfile.REGION, port = 1)
        probes += probeId
        return jobId
    }

    private fun lease(
        jobId: UUID,
        owner: String,
        leaseSeconds: Int = 120,
    ) {
        em
            .createNativeQuery(
                """
                UPDATE probe_check_jobs
                SET status = 'LEASED', lease_owner = :owner, lease_until = now() + make_interval(secs => :secs)
                WHERE id = :id
                """,
            ).setParameter("owner", owner)
            .setParameter("secs", leaseSeconds)
            .setParameter("id", jobId)
            .executeUpdate()
    }

    private fun probeOf(jobId: UUID): UUID =
        QuarkusTransaction.requiringNew().call {
            em.createNativeQuery("SELECT probe_id FROM probe_check_jobs WHERE id = :id").setParameter("id", jobId).singleResult as UUID
        }

    private fun job(jobId: UUID): JobRow =
        QuarkusTransaction.requiringNew().call {
            val row =
                em
                    .createNativeQuery("SELECT status, lease_owner, delivery_attempts FROM probe_check_jobs WHERE id = :id")
                    .setParameter("id", jobId)
                    .singleResult as Array<*>
            JobRow(row[0].toString(), row[1] as String?, (row[2] as Number).toInt())
        }

    private fun leaseEndsAfter(
        jobId: UUID,
        seconds: Int,
    ): Boolean =
        QuarkusTransaction.requiringNew().call {
            em
                .createNativeQuery(
                    "SELECT lease_until > now() + make_interval(secs => :secs) FROM probe_check_jobs WHERE id = :id",
                ).setParameter("secs", seconds)
                .setParameter("id", jobId)
                .singleResult as Boolean
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
        fail<Unit>("check now never blocked on the claimed job")
    }

    companion object {
        private const val CLAIMER = "it-claimer"
        private const val OTHER_WORKER = "it-other-worker"
    }
}
