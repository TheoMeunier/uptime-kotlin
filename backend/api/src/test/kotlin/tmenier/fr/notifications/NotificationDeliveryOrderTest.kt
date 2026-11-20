package tmenier.fr.notifications

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import tmenier.fr.common.enums.notifications.NotificationEvent
import tmenier.fr.databases.repositories.NotificationTaskRepository
import tmenier.fr.deployments.ApiOnlyProfile
import tmenier.fr.incidents.IncidentFixtures
import java.time.Duration
import java.time.Instant
import java.util.UUID

@QuarkusTest
@TestProfile(ApiOnlyProfile::class)
class NotificationDeliveryOrderTest {
    @Inject
    lateinit var incidentFixtures: IncidentFixtures

    @Inject
    lateinit var fixtures: DeliveryFixtures

    @Inject
    lateinit var repository: NotificationTaskRepository

    private val probes = mutableListOf<UUID>()
    private val channels = mutableListOf<UUID>()
    private val now = Instant.now()

    @AfterEach
    fun cleanUp() {
        probes.forEach(fixtures::deleteDeliveries)
        probes.forEach(incidentFixtures::delete)
        channels.forEach(fixtures::deleteChannel)
    }

    private fun claim(ours: Set<UUID>): Set<UUID> =
        repository.claimDueTasks("it-order-worker", 500, Duration.ofMinutes(2)).filter(ours::contains).toSet()

    @Test
    fun `a recovery is not sent while the failure before it is waiting for its retry`() {
        val probe = incidentFixtures.probe().also(probes::add)
        val channel = fixtures.channel().also(channels::add)
        val failure =
            fixtures.delivery(probe, channel, NotificationEvent.FAILURE, createdAt = now.minusSeconds(60), availableAt = now.plusSeconds(40))
        val recovery =
            fixtures.delivery(probe, channel, NotificationEvent.RECOVERY, createdAt = now.minusSeconds(5), availableAt = now.minusSeconds(5))

        assertEquals(emptySet<UUID>(), claim(setOf(failure, recovery)), "the recovery waits behind the failure")

        fixtures.makeDue(failure)
        assertEquals(setOf(failure), claim(setOf(failure, recovery)), "the failure goes first, on its own")

        assertEquals(emptySet<UUID>(), claim(setOf(failure, recovery)), "the failure is still being sent")

        repository.markSent(failure)
        assertEquals(setOf(recovery), claim(setOf(failure, recovery)), "then the recovery")
    }

    @Test
    fun `a dead failure does not block the recovery`() {
        val probe = incidentFixtures.probe().also(probes::add)
        val channel = fixtures.channel().also(channels::add)
        val failure = fixtures.delivery(probe, channel, NotificationEvent.FAILURE, createdAt = now.minusSeconds(60), availableAt = now)
        val recovery = fixtures.delivery(probe, channel, NotificationEvent.RECOVERY, createdAt = now.minusSeconds(5), availableAt = now)
        fixtures.markDead(failure)

        assertEquals(setOf(recovery), claim(setOf(failure, recovery)))
    }

    @Test
    fun `deliveries of other channels and other probes are not held up`() {
        val probe = incidentFixtures.probe().also(probes::add)
        val other = incidentFixtures.probe().also(probes::add)
        val discord = fixtures.channel().also(channels::add)
        val slack = fixtures.channel().also(channels::add)
        fixtures.delivery(probe, discord, NotificationEvent.FAILURE, createdAt = now.minusSeconds(60), availableAt = now.plusSeconds(40))
        val otherChannel = fixtures.delivery(probe, slack, NotificationEvent.RECOVERY, createdAt = now.minusSeconds(5), availableAt = now)
        val otherProbe = fixtures.delivery(other, discord, NotificationEvent.RECOVERY, createdAt = now.minusSeconds(5), availableAt = now)

        assertEquals(setOf(otherChannel, otherProbe), claim(setOf(otherChannel, otherProbe)))
    }
}

@ApplicationScoped
class DeliveryFixtures(
    private val em: EntityManager,
) {
    @Transactional
    fun channel(): UUID {
        val id = UUID.randomUUID()
        em
            .createNativeQuery("INSERT INTO notifications_channels (id, name, type) VALUES (:id, :name, 0)")
            .setParameter("id", id)
            .setParameter("name", "it-order-${id.toString().take(8)}")
            .executeUpdate()
        return id
    }

    @Transactional
    fun delivery(
        probeId: UUID,
        channelId: UUID,
        event: NotificationEvent,
        createdAt: Instant,
        availableAt: Instant,
    ): UUID {
        val id = UUID.randomUUID()
        em
            .createNativeQuery(
                """
                INSERT INTO notification_deliveries
                    (id, probe_id, notification_channel_id, event, status, payload, available_at, created_at)
                VALUES (:id, :probe, :channel, :event, 'PENDING', CAST('{}' AS jsonb), :availableAt, :createdAt)
                """.trimIndent(),
            ).setParameter("id", id)
            .setParameter("probe", probeId)
            .setParameter("channel", channelId)
            .setParameter("event", event.ordinal)
            .setParameter("availableAt", availableAt)
            .setParameter("createdAt", createdAt)
            .executeUpdate()
        return id
    }

    @Transactional
    fun makeDue(id: UUID) {
        em
            .createNativeQuery("UPDATE notification_deliveries SET available_at = now() - interval '1 second' WHERE id = :id")
            .setParameter("id", id)
            .executeUpdate()
    }

    @Transactional
    fun markDead(id: UUID) {
        em.createNativeQuery("UPDATE notification_deliveries SET status = 'DEAD' WHERE id = :id").setParameter("id", id).executeUpdate()
    }

    @Transactional
    fun deleteDeliveries(probeId: UUID) {
        em.createNativeQuery("DELETE FROM notification_deliveries WHERE probe_id = :id").setParameter("id", probeId).executeUpdate()
    }

    @Transactional
    fun deleteChannel(id: UUID) {
        em.createNativeQuery("DELETE FROM notifications_channels WHERE id = :id").setParameter("id", id).executeUpdate()
    }
}
