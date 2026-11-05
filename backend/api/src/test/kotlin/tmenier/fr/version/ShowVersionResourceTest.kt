package tmenier.fr.version

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.restassured.RestAssured.given
import jakarta.inject.Inject
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import tmenier.fr.deployments.ApiOnlyProfile
import tmenier.fr.users.UserFixtures
import tmenier.fr.version.resources.ShowVersionResource

@QuarkusTest
@TestProfile(ApiOnlyProfile::class)
class ShowVersionResourceTest {
    @Inject
    lateinit var users: UserFixtures

    @Test
    fun `a signed-in user gets the running version`() {
        given()
            .auth()
            .oauth2(users.token())
            .get("/api/version")
            .then()
            .statusCode(200)
            .body("version", equalTo("dev"))
    }

    @Test
    fun `the version is not public`() {
        given()
            .get("/api/version")
            .then()
            .statusCode(401)
    }

    @Test
    fun `the leading v of the git tag is removed`() {
        assertEquals("1.5.0", ShowVersionResource.normalize("v1.5.0"))
        assertEquals("1.5.0-rc.1", ShowVersionResource.normalize(" v1.5.0-rc.1 "))
        assertEquals("1.5.0", ShowVersionResource.normalize("1.5.0"))
        assertEquals("dev", ShowVersionResource.normalize("dev"))
        assertEquals("dev", ShowVersionResource.normalize(""))
        assertEquals("main", ShowVersionResource.normalize("main"))
    }
}
