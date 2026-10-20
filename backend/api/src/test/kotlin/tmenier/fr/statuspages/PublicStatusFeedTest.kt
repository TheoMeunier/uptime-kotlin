package tmenier.fr.statuspages

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import jakarta.ws.rs.core.Response
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tmenier.fr.announcements.actions.EndStatusAnnouncementAction
import tmenier.fr.announcements.actions.ListStatusAnnouncementAction
import tmenier.fr.announcements.actions.StoreStatusAnnouncementAction
import tmenier.fr.announcements.requests.StoreStatusAnnouncementRequest
import tmenier.fr.common.enums.statuspages.AnnouncementSeverity
import tmenier.fr.common.enums.statuspages.AnnouncementState
import tmenier.fr.common.exceptions.common.BadRequestException
import tmenier.fr.common.exceptions.common.NotFoundException
import tmenier.fr.common.exceptions.core.ApiException
import tmenier.fr.deployments.ApiOnlyProfile
import tmenier.fr.statuspages.actions.PublicStatusFeedAction
import tmenier.fr.statuspages.actions.StoreStatusPageAction
import tmenier.fr.statuspages.requests.StoreStatusPageGroupRequest
import tmenier.fr.statuspages.requests.StoreStatusPageRequest
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@QuarkusTest
@TestProfile(ApiOnlyProfile::class)
class PublicStatusFeedTest {
    @Inject
    lateinit var fixtures: StatusPageFixtures

    @Inject
    lateinit var storePage: StoreStatusPageAction

    @Inject
    lateinit var storeAnnouncement: StoreStatusAnnouncementAction

    @Inject
    lateinit var endAnnouncement: EndStatusAnnouncementAction

    @Inject
    lateinit var listAnnouncements: ListStatusAnnouncementAction

    @Inject
    lateinit var feed: PublicStatusFeedAction

    private val createdPages = mutableListOf<UUID>()
    private val createdAnnouncements = mutableListOf<UUID>()
    private val now = Instant.now().truncatedTo(ChronoUnit.SECONDS)

    @AfterEach
    fun cleanUp() {
        createdAnnouncements.forEach(fixtures::deleteAnnouncement)
        createdPages.forEach(fixtures::deletePage)
    }

    @Test
    fun `an announcement shows on its pages between its start and its end`() {
        val target = page()
        val other = page()
        val targetId = fixtures.pageId(target)

        val live = announce(pages = listOf(targetId), startsAt = now.minusSeconds(60))
        val scheduled = announce(pages = listOf(targetId), startsAt = now.plus(days(1)))
        val past = announce(pages = listOf(targetId), startsAt = now.minus(days(10)), endsAt = now.minus(days(9)))

        assertEquals(listOf(live), feed.forPage(target, now).announcements.map { it.id }, "only the one shown now")
        assertTrue(feed.forPage(other, now).announcements.isEmpty(), "other pages are not concerned")
        assertTrue(feed.forGlobal(now).announcements.none { it.id == live }, "not flagged for /status")

        val states = listAnnouncements.execute(now).associate { it.id to it.state }
        assertEquals(AnnouncementState.ACTIVE, states[live])
        assertEquals(AnnouncementState.SCHEDULED, states[scheduled])
        assertEquals(AnnouncementState.ENDED, states[past])
    }

    @Test
    fun `an announcement flagged for the global page shows on status`() {
        val id = announce(pages = emptyList(), global = true, startsAt = now.minusSeconds(60))

        assertTrue(feed.forGlobal(now).announcements.any { it.id == id })
    }

    @Test
    fun `ending an announcement hides it, twice is harmless, a scheduled one is refused`() {
        val slug = page()
        val pageId = fixtures.pageId(slug)
        val live = announce(pages = listOf(pageId), startsAt = now.minusSeconds(60))
        val scheduled = announce(pages = listOf(pageId), startsAt = now.plus(days(1)))

        val ended = endAnnouncement.execute(live, now)
        assertEquals(AnnouncementState.ENDED, ended.state)
        assertEquals(now, ended.endsAt)
        assertEquals(now, endAnnouncement.execute(live, now.plusSeconds(30)).endsAt, "the first end wins")

        val refused = assertThrows(ApiException::class.java) { endAnnouncement.execute(scheduled, now) }
        assertEquals(Response.Status.CONFLICT, refused.httpStatus)
        assertEquals(EndStatusAnnouncementAction.ANNOUNCEMENT_NOT_STARTED, refused.errorCode)

        assertTrue(feed.forPage(slug, now.plusSeconds(1)).announcements.isEmpty())
        assertThrows(NotFoundException::class.java) { endAnnouncement.execute(UUID.randomUUID(), now) }
    }

    @Test
    fun `an announcement needs a target, an end after its start, and known pages`() {
        val pageId = fixtures.pageId(page())

        assertThrows(BadRequestException::class.java) { announce(pages = emptyList(), global = false) }
        assertThrows(BadRequestException::class.java) { announce(pages = listOf(pageId), startsAt = now, endsAt = now) }
        assertThrows(BadRequestException::class.java) { announce(pages = listOf(UUID.randomUUID())) }
    }

    @Test
    fun `updating an announcement without a start keeps the current one, and its pages are replaced`() {
        val first = fixtures.pageId(page())
        val second = fixtures.pageId(page())
        val id = announce(pages = listOf(first), startsAt = now.minus(days(1)))

        storeAnnouncement.execute(
            request(title = "  Renamed  ", pages = listOf(second), startsAt = null),
            announcementId = id,
            now = now,
        )

        val updated = listAnnouncements.show(id, now)
        assertEquals("Renamed", updated.title)
        assertEquals(now.minus(days(1)), updated.startsAt)
        assertEquals(listOf(second), updated.pages.map { it.id })
    }

    @Test
    fun `an unknown page has no feed`() {
        assertThrows(NotFoundException::class.java) { feed.forPage("no-such-page-${UUID.randomUUID()}", now) }
    }

    private fun page(): String {
        val slug = "it-feed-${UUID.randomUUID().toString().take(8)}"
        val request =
            StoreStatusPageRequest().apply {
                this.slug = slug
                title = slug
                groups = listOf(StoreStatusPageGroupRequest())
            }
        storePage.execute(request).also(createdPages::add)
        return slug
    }

    private fun announce(
        pages: List<UUID>,
        global: Boolean = false,
        startsAt: Instant? = now,
        endsAt: Instant? = null,
    ): UUID =
        storeAnnouncement
            .execute(request(pages = pages, global = global, startsAt = startsAt, endsAt = endsAt), now = now)
            .also(createdAnnouncements::add)

    private fun request(
        title: String = "Degraded performance",
        pages: List<UUID>,
        global: Boolean = false,
        startsAt: Instant?,
        endsAt: Instant? = null,
    ) = StoreStatusAnnouncementRequest().apply {
        this.title = title
        body = "We are looking into it."
        severity = AnnouncementSeverity.DEGRADED
        this.startsAt = startsAt
        this.endsAt = endsAt
        showOnGlobal = global
        statusPageIds = pages
    }

    private fun days(count: Long) = Duration.ofDays(count)
}

@ApplicationScoped
class StatusPageFixtures(
    private val em: EntityManager,
) {
    @Transactional
    fun pageId(slug: String): UUID =
        em
            .createQuery("SELECT p.id FROM StatusPageEntity p WHERE p.slug = :slug", UUID::class.java)
            .setParameter("slug", slug)
            .singleResult

    @Transactional
    fun deletePage(id: UUID) {
        em.createNativeQuery("DELETE FROM status_pages WHERE id = :id").setParameter("id", id).executeUpdate()
    }

    @Transactional
    fun deleteAnnouncement(id: UUID) {
        em.createNativeQuery("DELETE FROM status_announcements WHERE id = :id").setParameter("id", id).executeUpdate()
    }

    /** One check log, written directly so that its time and status are exactly the ones the test needs. */
    @Transactional
    fun log(
        probeId: UUID,
        at: Instant,
        status: Int,
        underMaintenance: Boolean = false,
    ) {
        em
            .createNativeQuery(
                """
                INSERT INTO probes_monitors_logs (id, probe_id, status, response_time, message, run_at, under_maintenance)
                VALUES (:id, :probe, :status, 10, 'test', :at, :maintenance)
                """.trimIndent(),
            ).setParameter("id", UUID.randomUUID())
            .setParameter("probe", probeId)
            .setParameter("status", status)
            .setParameter("at", at)
            .setParameter("maintenance", underMaintenance)
            .executeUpdate()
    }

    @Transactional
    fun retention(
        probeId: UUID,
        days: Int,
    ) {
        em
            .createNativeQuery("UPDATE probes SET log_retention_days = :days WHERE id = :id")
            .setParameter("days", days)
            .setParameter("id", probeId)
            .executeUpdate()
    }
}
