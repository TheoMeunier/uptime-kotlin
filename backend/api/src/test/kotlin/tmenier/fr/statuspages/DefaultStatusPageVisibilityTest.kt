package tmenier.fr.statuspages

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import jakarta.inject.Inject
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.deployments.ApiOnlyProfile
import tmenier.fr.users.UserFixtures

@QuarkusTest
@TestProfile(ApiOnlyProfile::class)
class DefaultStatusPageVisibilityTest {
    @Inject
    lateinit var users: UserFixtures

    private val globalEndpoints = listOf("/api/probes/status", "/api/status-feed")

    @AfterEach
    fun restorePublic() {
        setPrivate(false)
    }

    @Test
    fun `the built-in board is public by default`() {
        signedIn()
            .get("/api/settings/status-page")
            .then()
            .statusCode(200)
            .body("is_private", equalTo(false))

        globalEndpoints.forEach { path ->
            given()
                .get(path)
                .then()
                .statusCode(200)
                .header("Cache-Control", containsString("public"))
        }
    }

    @Test
    fun `once private, the built-in board answers 404 to anonymous visitors`() {
        setPrivate(true).then().statusCode(200).body("is_private", equalTo(true))

        globalEndpoints.forEach { path ->
            given().get(path).then().statusCode(404)
        }
    }

    @Test
    fun `once private, the built-in board is still served to signed-in users, never to shared caches`() {
        setPrivate(true)

        globalEndpoints.forEach { path ->
            signedIn()
                .get(path)
                .then()
                .statusCode(200)
                .header("Cache-Control", containsString("private"))
        }
    }

    @Test
    fun `the 304 shortcut does not bypass the check`() {
        val etag =
            given()
                .get("/api/probes/status")
                .then()
                .statusCode(200)
                .extract()
                .header("ETag")

        setPrivate(true)

        given()
            .header("If-None-Match", etag)
            .get("/api/probes/status")
            .then()
            .statusCode(404)
    }

    @Test
    fun `a viewer cannot change the setting`() {
        given()
            .auth()
            .oauth2(users.token(UserRole.VIEWER))
            .contentType(ContentType.JSON)
            .body(mapOf("is_private" to true))
            .put("/api/settings/status-page")
            .then()
            .statusCode(403)
    }

    private fun signedIn(): RequestSpecification = given().auth().oauth2(users.token())

    private fun setPrivate(isPrivate: Boolean) =
        signedIn()
            .contentType(ContentType.JSON)
            .body(mapOf("is_private" to isPrivate))
            .put("/api/settings/status-page")
}
