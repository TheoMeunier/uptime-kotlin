package tmenier.fr.statuspages

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.restassured.RestAssured.given
import jakarta.inject.Inject
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import tmenier.fr.common.exceptions.common.BadRequestException
import tmenier.fr.deployments.ApiOnlyProfile
import tmenier.fr.statuspages.actions.StoreStatusPageAction
import tmenier.fr.statuspages.domains.CustomDomainRegistry
import tmenier.fr.statuspages.requests.StoreStatusPageRequest
import java.util.UUID

@QuarkusTest
@TestProfile(ApiOnlyProfile::class)
class CustomDomainTest {
    @Inject
    lateinit var fixtures: StatusPageFixtures

    @Inject
    lateinit var storePage: StoreStatusPageAction

    @Inject
    lateinit var registry: CustomDomainRegistry

    private val createdPages = mutableListOf<UUID>()

    @AfterEach
    fun cleanUp() {
        createdPages.forEach(fixtures::deletePage)
        registry.reload()
    }

    @Test
    fun `a custom domain resolves to its page, the main domain to nothing`() {
        val domain = domain()
        val slug = page(domain = domain)

        given()
            .header("Host", domain)
            .get("/api/status-domains/resolve")
            .then()
            .statusCode(200)
            .body("slug", equalTo(slug))
        given()
            .header("Host", domain.uppercase() + ":443")
            .get("/api/status-domains/resolve")
            .then()
            .statusCode(200)
        given().get("/api/status-domains/resolve").then().statusCode(404)
    }

    @Test
    fun `a forwarded host sent by a client that is not a trusted proxy is ignored`() {
        val domain = domain()
        page(domain = domain)

        given()
            .header("X-Forwarded-Host", domain)
            .get("/api/status-domains/resolve")
            .then()
            .statusCode(404)
        given()
            .header("X-Forwarded-Host", domain)
            .get("/api/probes")
            .then()
            .statusCode(401)
    }

    @Test
    fun `the on-demand TLS check only accepts declared domains`() {
        val domain = domain()
        page(domain = domain)

        given()
            .queryParam("domain", domain)
            .get("/api/status-domains/allowed")
            .then()
            .statusCode(200)
        given()
            .queryParam("domain", "unknown-${UUID.randomUUID()}.example.com")
            .get("/api/status-domains/allowed")
            .then()
            .statusCode(404)
        given().get("/api/status-domains/allowed").then().statusCode(404)
    }

    @Test
    fun `a custom domain serves its public page but not the rest of the API`() {
        val domain = domain()
        val slug = page(domain = domain)

        given()
            .header("Host", domain)
            .get("/api/status/$slug")
            .then()
            .statusCode(200)
        given()
            .header("Host", domain)
            .get("/api/probes")
            .then()
            .statusCode(404)
        given()
            .header("Host", domain)
            .contentType("application/json")
            .body("{}")
            .post("/api/auth/login")
            .then()
            .statusCode(404)
    }

    @Test
    fun `the domain is cleaned up and checked before saving`() {
        val domain = domain()
        page(domain = " ${domain.uppercase()}. ")

        given()
            .queryParam("domain", domain)
            .get("/api/status-domains/allowed")
            .then()
            .statusCode(200)

        assertThrows(BadRequestException::class.java) { page(domain = domain) }
        assertThrows(BadRequestException::class.java) { page(domain = "https://$domain") }
        assertThrows(BadRequestException::class.java) { page(domain = "192.168.1.10") }
        assertThrows(BadRequestException::class.java) { page(domain = domain(), isPrivate = true) }
        assertThrows(BadRequestException::class.java) {
            val host = domain()
            page(domain = host, currentHost = host)
        }
    }

    private fun domain() = "it-${UUID.randomUUID().toString().take(8)}.example.com"

    private fun page(
        domain: String?,
        isPrivate: Boolean = false,
        currentHost: String? = null,
    ): String {
        val slug = "it-domain-${UUID.randomUUID().toString().take(8)}"
        val request =
            StoreStatusPageRequest().apply {
                this.slug = slug
                title = slug
                this.isPrivate = isPrivate
                customDomain = domain
            }
        storePage.execute(request, currentHost = currentHost).also(createdPages::add)
        return slug
    }
}
