package tmenier.fr.users

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import jakarta.inject.Inject
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.databases.repositories.UserRepository
import tmenier.fr.deployments.ApiOnlyProfile
import java.util.UUID

@QuarkusTest
@TestProfile(ApiOnlyProfile::class)
class UserRolesTest {
    @Inject
    lateinit var users: UserFixtures

    @Inject
    lateinit var userRepository: UserRepository

    private val createdIds = mutableListOf<UUID>()
    private val createdEmails = mutableListOf<String>()

    @AfterEach
    fun cleanUp() {
        createdEmails.mapNotNull { userRepository.findByEmailOrNull(it)?.id }.forEach(users::delete)
        createdIds.forEach(users::delete)
    }

    @Test
    fun `an admin adds a viewer who can read but not write`() {
        val admin = user(UserRole.ADMIN)
        val email = "viewer-${UUID.randomUUID().toString().take(8)}@example.com".also(createdEmails::add)

        given()
            .auth()
            .oauth2(users.token(admin))
            .contentType(ContentType.JSON)
            .body(mapOf("name" to "Vera", "email" to email.uppercase(), "password" to "correct horse", "role" to "VIEWER"))
            .post("/api/users/new")
            .then()
            .statusCode(200)

        val token =
            given()
                .contentType(ContentType.JSON)
                .body(mapOf("email" to email, "password" to "correct horse"))
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path<String>("token")

        given()
            .auth()
            .oauth2(token)
            .get("/api/probes")
            .then()
            .statusCode(200)

        given()
            .auth()
            .oauth2(token)
            .contentType(ContentType.JSON)
            .body("{}")
            .post("/api/status-pages/new")
            .then()
            .statusCode(403)
            .body("error", equalTo("READ_ONLY_ACCOUNT"))

        given()
            .auth()
            .oauth2(token)
            .get("/api/users")
            .then()
            .statusCode(403)
    }

    @Test
    fun `only admins list the users`() {
        val admin = user(UserRole.ADMIN)
        val viewer = user(UserRole.VIEWER)

        given()
            .auth()
            .oauth2(users.token(admin))
            .get("/api/users")
            .then()
            .statusCode(200)
            .body("email", hasItem(viewer.email))

        given()
            .auth()
            .oauth2(users.token(viewer))
            .get("/api/users")
            .then()
            .statusCode(403)
    }

    @Test
    fun `a demoted admin loses admin rights on the next request`() {
        val admin = user(UserRole.ADMIN)
        val demoted = user(UserRole.ADMIN)
        val demotedToken = users.token(demoted)

        given()
            .auth()
            .oauth2(demotedToken)
            .get("/api/users")
            .then()
            .statusCode(200)

        given()
            .auth()
            .oauth2(users.token(admin))
            .contentType(ContentType.JSON)
            .body(mapOf("role" to "VIEWER"))
            .post("/api/users/${demoted.id}/role")
            .then()
            .statusCode(204)

        given()
            .auth()
            .oauth2(demotedToken)
            .get("/api/users")
            .then()
            .statusCode(403)
    }

    @Test
    fun `a removed user's token is refused immediately`() {
        val admin = user(UserRole.ADMIN)
        val removed = users.user(UserRole.VIEWER)
        val removedToken = users.token(removed)

        given()
            .auth()
            .oauth2(removedToken)
            .get("/api/probes")
            .then()
            .statusCode(200)

        given()
            .auth()
            .oauth2(users.token(admin))
            .post("/api/users/${removed.id}/remove")
            .then()
            .statusCode(204)

        given()
            .auth()
            .oauth2(removedToken)
            .get("/api/probes")
            .then()
            .statusCode(401)
    }

    @Test
    fun `an admin cannot change their own role or remove themselves`() {
        val admin = user(UserRole.ADMIN)
        val token = users.token(admin)

        given()
            .auth()
            .oauth2(token)
            .contentType(ContentType.JSON)
            .body(mapOf("role" to "VIEWER"))
            .post("/api/users/${admin.id}/role")
            .then()
            .statusCode(409)
            .body("error", equalTo("OWN_ACCOUNT"))

        given()
            .auth()
            .oauth2(token)
            .post("/api/users/${admin.id}/remove")
            .then()
            .statusCode(409)
    }

    @Test
    fun `an email already used cannot be added again`() {
        val admin = user(UserRole.ADMIN)
        val existing = user(UserRole.VIEWER)

        given()
            .auth()
            .oauth2(users.token(admin))
            .contentType(ContentType.JSON)
            .body(mapOf("name" to "Twin", "email" to existing.email, "password" to "correct horse", "role" to "ADMIN"))
            .post("/api/users/new")
            .then()
            .statusCode(409)
            .body("error", equalTo("EMAIL_ALREADY_USED"))
    }

    private fun user(role: UserRole) = users.user(role).also { createdIds.add(it.id) }
}
