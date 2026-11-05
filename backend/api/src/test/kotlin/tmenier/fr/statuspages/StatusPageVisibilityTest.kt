package tmenier.fr.statuspages

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.restassured.RestAssured.given
import io.restassured.specification.RequestSpecification
import jakarta.inject.Inject
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import tmenier.fr.deployments.ApiOnlyProfile
import tmenier.fr.incidents.IncidentFixtures
import tmenier.fr.statuspages.actions.StoreStatusPageAction
import tmenier.fr.statuspages.requests.StoreStatusPageGroupRequest
import tmenier.fr.statuspages.requests.StoreStatusPageRequest
import tmenier.fr.users.UserFixtures
import java.util.UUID

@QuarkusTest
@TestProfile(ApiOnlyProfile::class)
class StatusPageVisibilityTest {
    @Inject
    lateinit var users: UserFixtures

    @Inject
    lateinit var probes: IncidentFixtures

    @Inject
    lateinit var fixtures: StatusPageFixtures

    @Inject
    lateinit var storePage: StoreStatusPageAction

    private val createdProbes = mutableListOf<UUID>()
    private val createdPages = mutableListOf<UUID>()

    @AfterEach
    fun cleanUp() {
        createdPages.forEach(fixtures::deletePage)
        createdProbes.forEach(probes::delete)
    }

    @Test
    fun `a private page answers 404 to anonymous visitors, on every public endpoint`() {
        val slug = page(isPrivate = true)

        listOf("/api/status/$slug", "/api/status/$slug/uptime", "/api/status/$slug/feed").forEach { path ->
            given().get(path).then().statusCode(404)
        }
    }

    @Test
    fun `a private page is served to signed-in users, never to shared caches`() {
        val slug = page(isPrivate = true)

        listOf("/api/status/$slug", "/api/status/$slug/uptime", "/api/status/$slug/feed").forEach { path ->
            signedIn()
                .get(path)
                .then()
                .statusCode(200)
                .header("Cache-Control", containsString("private"))
                .header("Vary", containsString("Authorization"))
        }
    }

    @Test
    fun `a public page stays open to everyone and cacheable`() {
        val slug = page(isPrivate = false)

        given()
            .get("/api/status/$slug")
            .then()
            .statusCode(200)
            .header("Cache-Control", containsString("public"))
    }

    @Test
    fun `the 304 shortcut does not bypass the check`() {
        val slug = page(isPrivate = true)
        val etag =
            signedIn()
                .get("/api/status/$slug")
                .then()
                .statusCode(200)
                .extract()
                .header("ETag")

        given()
            .header("If-None-Match", etag)
            .get("/api/status/$slug")
            .then()
            .statusCode(404)
    }

    @Test
    fun `the global board hides from anonymous visitors the probes shown only on private pages`() {
        val privateOnly = probe()
        val onBoth = probe()
        page(isPrivate = true, privateOnly, onBoth)
        page(isPrivate = false, onBoth)

        given()
            .get("/api/probes/status")
            .then()
            .statusCode(200)
            .body("probe.id", not(hasItem(privateOnly.toString())))
            .body("probe.id", hasItem(onBoth.toString()))

        signedIn()
            .get("/api/probes/status")
            .then()
            .statusCode(200)
            .header("Cache-Control", containsString("private"))
            .body("probe.id", hasItem(privateOnly.toString()))
    }

    @Test
    fun `the private flag is saved and returned to the editor`() {
        val slug = page(isPrivate = true)
        val id = fixtures.pageId(slug)

        signedIn()
            .get("/api/status-pages/$id")
            .then()
            .statusCode(200)
            .body("is_private", equalTo(true))
    }

    private fun signedIn(): RequestSpecification = given().auth().oauth2(users.token())

    private fun probe(): UUID = probes.probe().also(createdProbes::add)

    private fun page(
        isPrivate: Boolean,
        vararg probeIds: UUID,
    ): String {
        val slug = "it-visibility-${UUID.randomUUID().toString().take(8)}"
        val request =
            StoreStatusPageRequest().apply {
                this.slug = slug
                title = slug
                this.isPrivate = isPrivate
                groups = listOf(StoreStatusPageGroupRequest().apply { this.probeIds = probeIds.toList() })
            }
        storePage.execute(request).also(createdPages::add)
        return slug
    }
}
