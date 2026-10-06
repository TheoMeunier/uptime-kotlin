package tmenier.fr.deployments

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.restassured.RestAssured.given
import io.smallrye.jwt.build.Jwt
import io.smallrye.jwt.util.KeyUtils
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import tmenier.fr.auth.services.JwtService
import tmenier.fr.databases.repositories.JwtSigningKeyRepository
import tmenier.fr.databases.repositories.StoredJwtSigningKey
import java.time.Instant
import java.util.Base64
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@QuarkusTest
@TestProfile(ApiOnlyProfile::class)
class JwtSigningKeyTest {
    @Inject
    lateinit var jwtService: JwtService

    @Inject
    lateinit var repository: JwtSigningKeyRepository

    @Inject
    lateinit var fixtures: JwtSigningKeyFixtures

    @Test
    fun `a token signed by the API is accepted on an authenticated endpoint`() {
        val token = jwtService.generateJwt(UUID.randomUUID(), "Jane Doe", "jane@example.com")

        given()
            .auth()
            .oauth2(token)
            .get("/api/probes")
            .then()
            .statusCode(200)
    }

    @Test
    fun `an access token is only valid for fifteen minutes`() {
        val token = jwtService.generateJwt(UUID.randomUUID(), "Jane Doe", "jane@example.com")
        val payload = String(Base64.getUrlDecoder().decode(token.split('.')[1]))

        val issuedAt = Regex("\"iat\":(\\d+)").find(payload)!!.groupValues[1].toLong()
        val expiresAt = Regex("\"exp\":(\\d+)").find(payload)!!.groupValues[1].toLong()

        assertEquals(15 * 60L, expiresAt - issuedAt)
    }

    @Test
    fun `a token signed with another key is rejected`() {
        val foreignKey = KeyUtils.generateKeyPair(2048).private
        val token =
            Jwt
                .claims()
                .subject(UUID.randomUUID().toString())
                .issuer("https://uptime-kotlin.theomeunier.fr/")
                .expiresAt(Instant.now().plusSeconds(600))
                .sign(foreignKey)

        given()
            .auth()
            .oauth2(token)
            .get("/api/probes")
            .then()
            .statusCode(401)
    }

    @Test
    fun `two instances starting together end up with the same key`() {
        val original = fixtures.take()
        val bothGenerating = CountDownLatch(2)
        val executor = Executors.newFixedThreadPool(2)

        try {
            val results =
                listOf("a", "b")
                    .map { name ->
                        executor.submit<StoredJwtSigningKey> {
                            repository.findOrCreate {
                                bothGenerating.countDown()
                                bothGenerating.await(5, TimeUnit.SECONDS)
                                StoredJwtSigningKey(publicKey = "public-$name", privateKey = "private-$name")
                            }
                        }
                    }.map { it.get(30, TimeUnit.SECONDS) }

            assertEquals(0, bothGenerating.count, "Both instances must have generated a candidate key")
            assertEquals(results[0], results[1])
            assertEquals(results[0], fixtures.current())
        } finally {
            executor.shutdownNow()
            fixtures.restore(original)
        }
    }
}

@ApplicationScoped
class JwtSigningKeyFixtures {
    @Inject
    lateinit var em: EntityManager

    @Transactional
    fun take(): StoredJwtSigningKey? = current().also { em.createNativeQuery("DELETE FROM jwt_signing_keys").executeUpdate() }

    @Transactional
    fun current(): StoredJwtSigningKey? {
        val row =
            em
                .createNativeQuery("SELECT public_key, private_key FROM jwt_signing_keys WHERE id = 1")
                .resultList
                .firstOrNull() as Array<*>? ?: return null

        return StoredJwtSigningKey(publicKey = row[0] as String, privateKey = row[1] as String)
    }

    @Transactional
    fun restore(key: StoredJwtSigningKey?) {
        em.createNativeQuery("DELETE FROM jwt_signing_keys").executeUpdate()
        if (key == null) return

        em
            .createNativeQuery("INSERT INTO jwt_signing_keys (id, public_key, private_key) VALUES (1, :publicKey, :privateKey)")
            .setParameter("publicKey", key.publicKey)
            .setParameter("privateKey", key.privateKey)
            .executeUpdate()
    }
}
