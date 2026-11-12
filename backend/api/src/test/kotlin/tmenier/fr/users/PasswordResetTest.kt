package tmenier.fr.users

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import jakarta.inject.Inject
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.databases.mappers.UserDto
import tmenier.fr.databases.repositories.UserRepository
import tmenier.fr.deployments.ApiOnlyProfile
import java.util.UUID

@QuarkusTest
@TestProfile(ApiOnlyProfile::class)
class PasswordResetTest {
    @Inject
    lateinit var users: UserFixtures

    @Inject
    lateinit var userRepository: UserRepository

    private val createdIds = mutableListOf<UUID>()

    @AfterEach
    fun cleanUp() {
        createdIds.forEach(users::delete)
    }

    private fun userWithPassword(
        role: UserRole,
        password: String,
    ): UserDto = users.user(role, password).also { createdIds += it.id }

    private fun login(
        email: String,
        password: String,
    ) = given()
        .contentType(ContentType.JSON)
        .body(mapOf("email" to email, "password" to password))
        .post("/api/auth/login")

    @Test
    fun `an admin resets a password, which must be changed before anything else`() {
        val admin = users.user(UserRole.ADMIN).also { createdIds += it.id }
        val viewer = userWithPassword(UserRole.VIEWER, "old password")

        val temporary =
            given()
                .auth()
                .oauth2(users.token(admin))
                .post("/api/users/${viewer.id}/reset-password")
                .then()
                .statusCode(200)
                .header("Cache-Control", "no-store")
                .extract()
                .path<String>("password")

        login(viewer.email, "old password").then().statusCode(401)

        val token =
            login(viewer.email, temporary)
                .then()
                .statusCode(200)
                .extract()
                .path<String>("token")

        given()
            .auth()
            .oauth2(token)
            .get("/api/probes")
            .then()
            .statusCode(403)
            .body("error", equalTo("PASSWORD_CHANGE_REQUIRED"))

        given()
            .auth()
            .oauth2(token)
            .contentType(ContentType.JSON)
            .body(
                mapOf(
                    "current_password" to temporary,
                    "password" to "brand new password",
                    "password_confirmation" to "brand new password",
                ),
            ).post("/api/profile/update/password")
            .then()
            .statusCode(200)

        assertFalse(userRepository.findByEmailIgnoreCaseOrNull(viewer.email)!!.mustChangePassword)

        val fresh =
            login(viewer.email, "brand new password")
                .then()
                .statusCode(200)
                .extract()
                .path<String>("token")

        given()
            .auth()
            .oauth2(fresh)
            .get("/api/probes")
            .then()
            .statusCode(200)
    }

    @Test
    fun `the list shows a pending reset`() {
        val admin = users.user(UserRole.ADMIN).also { createdIds += it.id }
        val viewer = userWithPassword(UserRole.VIEWER, "old password")

        given()
            .auth()
            .oauth2(users.token(admin))
            .post("/api/users/${viewer.id}/reset-password")
            .then()
            .statusCode(200)

        val pending =
            given()
                .auth()
                .oauth2(users.token(admin))
                .get("/api/users")
                .then()
                .statusCode(200)
                .extract()
                .path<Boolean>("find { it.id == '${viewer.id}' }.must_change_password")

        assertTrue(pending)
    }

    @Test
    fun `an admin cannot reset their own password here`() {
        val admin = users.user(UserRole.ADMIN).also { createdIds += it.id }

        given()
            .auth()
            .oauth2(users.token(admin))
            .post("/api/users/${admin.id}/reset-password")
            .then()
            .statusCode(409)
            .body("error", equalTo("OWN_PASSWORD"))
    }

    @Test
    fun `a viewer cannot reset anyone`() {
        val viewer = users.user(UserRole.VIEWER).also { createdIds += it.id }
        val other = users.user(UserRole.VIEWER).also { createdIds += it.id }

        given()
            .auth()
            .oauth2(users.token(viewer))
            .post("/api/users/${other.id}/reset-password")
            .then()
            .statusCode(403)
    }
}
