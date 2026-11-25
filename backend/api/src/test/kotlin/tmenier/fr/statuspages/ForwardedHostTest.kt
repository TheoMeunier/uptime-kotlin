package tmenier.fr.statuspages

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.restassured.RestAssured.given
import jakarta.inject.Inject
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import tmenier.fr.deployments.TrustedProxyProfile
import tmenier.fr.deployments.UntrustedProxyProfile
import tmenier.fr.statuspages.actions.StoreStatusPageAction
import tmenier.fr.statuspages.domains.CustomDomainRegistry
import tmenier.fr.statuspages.requests.StoreStatusPageRequest
import java.util.UUID

abstract class ForwardedHostTestBase {
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

    protected fun domain() = "it-${UUID.randomUUID().toString().take(8)}.example.com"

    protected fun page(domain: String): String {
        val slug = "it-fwd-${UUID.randomUUID().toString().take(8)}"
        val request =
            StoreStatusPageRequest().apply {
                this.slug = slug
                title = slug
                isPrivate = false
                customDomain = domain
            }
        storePage.execute(request).also(createdPages::add)
        return slug
    }
}

@QuarkusTest
@TestProfile(TrustedProxyProfile::class)
class TrustedProxyForwardedHostTest : ForwardedHostTestBase() {
    @Test
    fun `a trusted proxy chooses the host with X-Forwarded-Host`() {
        val domain = domain()
        val slug = page(domain)

        given()
            .header("X-Forwarded-Host", domain)
            .get("/api/status-domains/resolve")
            .then()
            .statusCode(200)
            .body("slug", equalTo(slug))
        given()
            .header("X-Forwarded-Host", domain)
            .get("/api/probes")
            .then()
            .statusCode(404)
    }
}

@QuarkusTest
@TestProfile(UntrustedProxyProfile::class)
class UntrustedProxyForwardedHostTest : ForwardedHostTestBase() {
    @Test
    fun `X-Forwarded-Host from an address outside the trusted proxies is ignored`() {
        val domain = domain()
        page(domain)

        given()
            .header("X-Forwarded-Host", domain)
            .get("/api/status-domains/resolve")
            .then()
            .statusCode(404)
        given()
            .header("Host", domain)
            .get("/api/status-domains/resolve")
            .then()
            .statusCode(200)
    }
}
