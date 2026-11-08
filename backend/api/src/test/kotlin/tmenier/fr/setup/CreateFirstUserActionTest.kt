package tmenier.fr.setup

import jakarta.persistence.EntityManager
import jakarta.validation.Validation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tmenier.fr.common.bcrypt.BcryptService
import tmenier.fr.databases.mappers.UserDto
import tmenier.fr.databases.repositories.AdvisoryLockRepository
import tmenier.fr.databases.repositories.UserRepository
import tmenier.fr.setup.actions.CreateFirstUserAction
import tmenier.fr.setup.dto.CreateFirstUserRequest
import java.lang.reflect.Proxy

class CreateFirstUserActionTest {
    private val validator = Validation.buildDefaultValidatorFactory().validator

    private fun fields(request: CreateFirstUserRequest): Set<String> = validator.validate(request).map { it.propertyPath.toString() }.toSet()

    @Test
    fun `rejects a blank name, an invalid email and a too short password`() {
        val violations = fields(CreateFirstUserRequest(name = " ", email = "not-an-email", password = "x", passwordConfirmation = "x"))

        assertTrue(violations.containsAll(setOf("name", "email", "password", "passwordConfirmation")))
    }

    @Test
    fun `rejects a name or an email longer than 255 characters`() {
        val violations =
            fields(
                CreateFirstUserRequest(
                    name = "a".repeat(256),
                    email = "a".repeat(250) + "@example.com",
                    password = VALID_PASSWORD,
                    passwordConfirmation = VALID_PASSWORD,
                ),
            )

        assertTrue(violations.containsAll(setOf("name", "email")))
    }

    @Test
    fun `accepts a valid request`() {
        assertTrue(fields(validRequest()).isEmpty())
    }

    @Test
    fun `creates the first user with a normalised email`() {
        val users = FakeUserRepository()

        assertTrue(buildAction(users).execute(validRequest(email = "  Admin@Example.COM ")))

        assertEquals(1, users.stored.size)
        assertEquals("admin@example.com", users.stored.single().email)
        assertEquals("Admin", users.stored.single().name)
    }

    @Test
    fun `does not create a user when another setup request holds the lock`() {
        val users = FakeUserRepository()

        assertFalse(buildAction(users, lockAvailable = false).execute(validRequest()))

        assertTrue(users.stored.isEmpty())
    }

    @Test
    fun `does not create a user once one exists`() {
        val users = FakeUserRepository(existing = 1)

        assertFalse(buildAction(users).execute(validRequest()))

        assertTrue(users.stored.isEmpty())
    }

    @Test
    fun `does not take the lock when the passwords differ`() {
        val lock = FakeAdvisoryLockRepository(available = true)
        val action = CreateFirstUserAction(BcryptService(), FakeUserRepository(), lock)

        assertFalse(action.execute(validRequest().copy(passwordConfirmation = "another-password")))

        assertEquals(0, lock.attempts)
    }

    private fun buildAction(
        users: FakeUserRepository,
        lockAvailable: Boolean = true,
    ) = CreateFirstUserAction(BcryptService(), users, FakeAdvisoryLockRepository(lockAvailable))

    private fun validRequest(email: String = "admin@example.com") =
        CreateFirstUserRequest(
            name = " Admin ",
            email = email,
            password = VALID_PASSWORD,
            passwordConfirmation = VALID_PASSWORD,
        )

    private companion object {
        const val VALID_PASSWORD = "correct-horse-battery"
    }
}

private class FakeUserRepository(
    private val existing: Long = 0,
) : UserRepository() {
    val stored = mutableListOf<UserDto>()

    override fun countAll(): Long = existing + stored.size

    override fun store(dto: UserDto) {
        stored += dto
    }
}

private class FakeAdvisoryLockRepository(
    private val available: Boolean,
) : AdvisoryLockRepository(unusedEntityManager()) {
    final var attempts = 0
        private set

    override fun tryLock(key: Long): Boolean {
        attempts++
        assertEquals(FIRST_USER_SETUP, key)
        return available
    }
}

private fun unusedEntityManager(): EntityManager =
    Proxy.newProxyInstance(EntityManager::class.java.classLoader, arrayOf(EntityManager::class.java)) { _, method, _ ->
        throw UnsupportedOperationException("EntityManager.${method.name} should not be called")
    } as EntityManager
