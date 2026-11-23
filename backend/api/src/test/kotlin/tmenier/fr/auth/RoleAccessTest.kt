package tmenier.fr.auth

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import jakarta.inject.Inject
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.deployments.ApiOnlyProfile
import tmenier.fr.users.UserFixtures
import java.util.UUID

@QuarkusTest
@TestProfile(ApiOnlyProfile::class)
class RoleAccessTest {
    @Inject
    lateinit var users: UserFixtures

    private val createdIds = mutableListOf<UUID>()
    private val id = UUID.randomUUID()

    @AfterEach
    fun cleanUp() {
        createdIds.forEach(users::delete)
    }

    @Test
    fun `a viewer reads the monitoring data`() {
        val token = token(UserRole.VIEWER)

        listOf(
            "/api/probes",
            "/api/probes/$id",
            "/api/incidents",
            "/api/incidents/export",
            "/api/maintenances",
            "/api/status-pages",
            "/api/notifications",
            "/api/dashboard/stats",
            "/api/auth/sessions",
            "/api/profile/two-factor",
            "/api/version",
        ).forEach { path ->
            given()
                .auth()
                .oauth2(token)
                .get(path)
                .then()
                .statusCode(not(403))
        }
    }

    @Test
    fun `a viewer manages their own account`() {
        val token = token(UserRole.VIEWER)

        listOf(
            "POST" to "/api/auth/sessions/revoke-all",
            "DELETE" to "/api/auth/sessions/$id",
            "POST" to "/api/profile/update",
            "POST" to "/api/profile/update/password",
            "POST" to "/api/profile/onboarding/complete",
            "POST" to "/api/profile/two-factor/enable",
            "POST" to "/api/profile/two-factor/disable",
            "POST" to "/api/profile/two-factor/recovery-codes",
        ).forEach { (method, path) ->
            given()
                .auth()
                .oauth2(token)
                .contentType(ContentType.JSON)
                .body("{}")
                .request(method, path)
                .then()
                .statusCode(not(403))
        }
    }

    @Test
    fun `a viewer changes nothing else`() {
        val token = token(UserRole.VIEWER)

        listOf(
            "POST" to "/api/probes/new",
            "POST" to "/api/probes/$id/update",
            "POST" to "/api/probes/$id/update-on-off",
            "POST" to "/api/probes/$id/check-now",
            "POST" to "/api/probes/$id/logs/purge",
            "POST" to "/api/incidents/$id/acknowledge",
            "POST" to "/api/notifications/testing",
            "POST" to "/api/status-pages/new",
            "POST" to "/api/maintenances/new",
            "PUT" to "/api/settings/retention",
            "PUT" to "/api/settings/status-page",
            "POST" to "/api/users/new",
            "POST" to "/api/backup/restore",
            "POST" to "/api/users/$id/disable-two-factor",
        ).forEach { (method, path) ->
            given()
                .auth()
                .oauth2(token)
                .contentType(ContentType.JSON)
                .body("{}")
                .request(method, path)
                .then()
                .statusCode(403)
                .body("error", equalTo("READ_ONLY_ACCOUNT"))
        }
    }

    @Test
    fun `a viewer sees neither monitor secrets, users nor backups`() {
        val token = token(UserRole.VIEWER)

        listOf("/api/probes/$id/edit", "/api/users", "/api/backup").forEach { path ->
            given()
                .auth()
                .oauth2(token)
                .get(path)
                .then()
                .statusCode(403)
        }
    }

    @Test
    fun `an admin reaches the admin endpoints`() {
        val token = token(UserRole.ADMIN)

        listOf("/api/probes/$id/edit", "/api/users", "/api/settings/retention").forEach { path ->
            given()
                .auth()
                .oauth2(token)
                .get(path)
                .then()
                .statusCode(not(403))
        }
    }

    @Test
    fun `public endpoints need no token`() {
        given().get("/api/app/status").then().statusCode(200)
    }

    private fun token(role: UserRole): String = users.user(role).also { createdIds += it.id }.let(users::token)
}
