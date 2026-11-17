package tmenier.fr.backup

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.restassured.path.json.JsonPath
import io.restassured.specification.RequestSpecification
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.deployments.ApiOnlyProfile
import tmenier.fr.users.UserFixtures
import java.util.UUID

@QuarkusTest
@TestProfile(ApiOnlyProfile::class)
class BackupRestoreTest {
    @Inject
    lateinit var users: UserFixtures

    @Inject
    lateinit var fixtures: BackupFixtures

    private val prefix = "it-backup-${UUID.randomUUID().toString().take(8)}"

    @AfterEach
    fun cleanUp() = fixtures.deleteByPrefix(prefix)

    @Test
    fun `a dry run reports what would be created and writes nothing`() {
        val report = restore(file(), dryRun = true)

        assertEquals(listOf("CREATE", "CREATE", "CREATE"), report.getList<String>("items.outcome"))
        assertEquals(true, report.getBoolean("dry_run"))
        assertEquals(0, fixtures.probeCount(prefix))
    }

    @Test
    fun `a restore creates the items and links them through their refs`() {
        restore(file(), dryRun = false)

        assertEquals(1, fixtures.probeCount(prefix))
        assertEquals(listOf("$prefix channel"), fixtures.probeChannels("$prefix monitor"))
        assertEquals(listOf("$prefix monitor"), fixtures.pageProbes(prefix))
    }

    @Test
    fun `the same file again is skipped, or renamed on request`() {
        restore(file(), dryRun = false)

        val skipped = restore(file(), dryRun = false)
        assertEquals(listOf("SKIP", "SKIP", "SKIP"), skipped.getList<String>("items.outcome"))
        assertEquals(1, fixtures.probeCount(prefix))

        val renamed = restore(file(), dryRun = false, onConflict = "rename")
        assertEquals(listOf("RENAME", "RENAME", "RENAME"), renamed.getList<String>("items.outcome"))
        assertEquals(listOf("$prefix channel (2)", "$prefix monitor (2)", "$prefix-2"), renamed.getList<String>("items.new_name"))
        assertEquals(listOf("$prefix channel (2)"), fixtures.probeChannels("$prefix monitor (2)"), "the copy links to the copy")
    }

    @Test
    fun `an invalid item is reported and left out, the others are restored`() {
        val report = restore(file(probeUrl = "not a url"), dryRun = false)

        assertEquals(listOf("CREATE", "ERROR", "CREATE"), report.getList<String>("items.outcome"))
        assertEquals(0, fixtures.probeCount(prefix))
        assertEquals(listOf("UNKNOWN_REFERENCE"), report.getList<String>("items[2].issues.code"), "the link to the left-out monitor is dropped")
    }

    @Test
    fun `secrets are backed up encrypted unless asked in plain text`() {
        restore(file(), dryRun = false)

        val encrypted = download("encrypted").getString("notifications.find { it.name == '$prefix channel' }.app_token")
        val plain = download("plain").getString("notifications.find { it.name == '$prefix channel' }.app_token")

        assertTrue(encrypted.startsWith("enc:v2:"), encrypted)
        assertEquals("gotify-secret-token", plain)
    }

    @Test
    fun `a backed-up monitor restores as is`() {
        restore(file(), dryRun = false)
        val backup = download("encrypted")
        val document =
            mapOf(
                "format" to 1,
                "notifications" to backup.getList<Map<String, Any?>>("notifications.findAll { it.name == '$prefix channel' }"),
                "probes" to backup.getList<Map<String, Any?>>("probes.findAll { it.name == '$prefix monitor' }"),
            )

        val report = restore(document, dryRun = true, onConflict = "rename")

        assertEquals(listOf("RENAME", "RENAME"), report.getList<String>("items.outcome"), report.prettify())
    }

    @Test
    fun `a viewer can neither back up nor restore`() {
        val viewer = given().auth().oauth2(users.token(UserRole.VIEWER))

        viewer.get("/api/backup").then().statusCode(403)
        viewer
            .contentType(ContentType.JSON)
            .body(file())
            .post("/api/backup/restore")
            .then()
            .statusCode(403)
    }

    @Test
    fun `a file from a newer version is refused`() {
        admin()
            .contentType(ContentType.JSON)
            .body(mapOf("format" to 99))
            .post("/api/backup/restore")
            .then()
            .statusCode(400)
    }

    private fun file(probeUrl: String = "https://example.com/health"): Map<String, Any?> =
        mapOf(
            "format" to 1,
            "notifications" to
                listOf(
                    mapOf(
                        "ref" to "channel-1",
                        "name" to "$prefix channel",
                        "notification_type" to "GOTIFY",
                        "is_default" to false,
                        "server_url" to "https://gotify.example.com",
                        "app_token" to "gotify-secret-token",
                    ),
                ),
            "probes" to
                listOf(
                    mapOf(
                        "ref" to "probe-1",
                        "name" to "$prefix monitor",
                        "protocol" to "HTTP",
                        "url" to probeUrl,
                        "http_code_allowed" to listOf("200-299"),
                        "notification_certificate" to false,
                        "ignore_certificate_errors" to false,
                        "interval" to 600,
                        "interval_retry" to 60,
                        "retry" to 1,
                        "timeout" to 5,
                        "enabled" to false,
                        "notifications" to listOf("channel-1"),
                    ),
                ),
            "status_pages" to
                listOf(
                    mapOf(
                        "ref" to "page-1",
                        "slug" to prefix,
                        "title" to "$prefix page",
                        "groups" to listOf(mapOf("name" to "Services", "probes" to listOf("probe-1"))),
                    ),
                ),
        )

    private fun restore(
        document: Map<String, Any?>,
        dryRun: Boolean,
        onConflict: String = "skip",
    ): JsonPath =
        admin()
            .contentType(ContentType.JSON)
            .queryParam("dry_run", dryRun)
            .queryParam("on_conflict", onConflict)
            .body(document)
            .post("/api/backup/restore")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()

    private fun download(secrets: String): JsonPath =
        admin()
            .queryParam("secrets", secrets)
            .get("/api/backup")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()

    private fun admin(): RequestSpecification = given().auth().oauth2(users.token())
}

@ApplicationScoped
class BackupFixtures(
    private val em: EntityManager,
) {
    @Transactional
    fun probeCount(prefix: String): Int =
        em
            .createQuery("SELECT COUNT(p) FROM ProbesEntity p WHERE p.name LIKE :name", Long::class.javaObjectType)
            .setParameter("name", "$prefix%")
            .singleResult
            .toInt()

    @Transactional
    fun probeChannels(probeName: String): List<String> =
        em
            .createQuery(
                "SELECT n.name FROM ProbesEntity p JOIN p.notifications n WHERE p.name = :name ORDER BY n.name",
                String::class.java,
            ).setParameter("name", probeName)
            .resultList

    @Transactional
    fun pageProbes(slug: String): List<String> =
        em
            .createQuery(
                "SELECT p.name FROM StatusPageEntity s JOIN s.groups g JOIN g.probes p WHERE s.slug = :slug",
                String::class.java,
            ).setParameter("slug", slug)
            .resultList

    @Transactional
    fun deleteByPrefix(prefix: String) {
        val like = "$prefix%"
        em.createNativeQuery("DELETE FROM status_pages WHERE slug LIKE :like").setParameter("like", like).executeUpdate()
        em
            .createNativeQuery("DELETE FROM probes_monitors_logs WHERE probe_id IN (SELECT id FROM probes WHERE name LIKE :like)")
            .setParameter("like", like)
            .executeUpdate()
        em.createNativeQuery("DELETE FROM probes WHERE name LIKE :like").setParameter("like", like).executeUpdate()
        em.createNativeQuery("DELETE FROM notifications_channels WHERE name LIKE :like").setParameter("like", like).executeUpdate()
    }
}
