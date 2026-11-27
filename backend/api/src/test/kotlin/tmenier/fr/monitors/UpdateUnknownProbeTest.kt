package tmenier.fr.monitors

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import jakarta.inject.Inject
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.deployments.ApiOnlyProfile
import tmenier.fr.users.UserFixtures
import java.util.UUID

@QuarkusTest
@TestProfile(ApiOnlyProfile::class)
class UpdateUnknownProbeTest {
    @Inject
    lateinit var users: UserFixtures

    private val createdIds = mutableListOf<UUID>()

    @AfterEach
    fun cleanUp() = createdIds.forEach(users::delete)

    @Test
    fun `updating an unknown Probe is a generic 404`() {
        val unknownId = UUID.randomUUID()
        val token = users.user(UserRole.ADMIN).also { createdIds += it.id }.let(users::token)

        given()
            .auth()
            .oauth2(token)
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "protocol": "TCP",
                  "name": "unknown",
                  "url": "localhost",
                  "tcp_port": 1,
                  "interval": 60,
                  "retry": 1,
                  "interval_retry": 60,
                  "enabled": true,
                  "notifications": ["${UUID.randomUUID()}"]
                }
                """.trimIndent(),
            ).post("/api/probes/$unknownId/update")
            .then()
            .statusCode(404)
            .body("error", equalTo("NOT_FOUND"))
            .body("message", equalTo("The requested resource was not found."))
    }
}
