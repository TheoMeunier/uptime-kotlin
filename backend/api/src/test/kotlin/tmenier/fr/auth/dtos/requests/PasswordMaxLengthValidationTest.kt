package tmenier.fr.auth.dtos.requests

import jakarta.validation.Validation
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tmenier.fr.profile.dtos.requests.UpdatePasswordRequest
import tmenier.fr.setup.dto.CreateFirstUserRequest
import tmenier.fr.users.requests.StoreUserRequest

class PasswordMaxLengthValidationTest {
    private val validator = Validation.buildDefaultValidatorFactory().validator
    private val max = "a".repeat(72)
    private val tooLong = "a".repeat(73)

    private fun fields(target: Any): Set<String> = validator.validate(target).map { it.propertyPath.toString() }.toSet()

    @Test
    fun `login rejects a password longer than 72 characters`() {
        assertTrue("password" in fields(LoginRequest(email = "a@example.com", password = tooLong)))
        assertFalse("password" in fields(LoginRequest(email = "a@example.com", password = max)))
    }

    @Test
    fun `first user creation rejects a password longer than 72 characters`() {
        val violations = fields(CreateFirstUserRequest("Admin", "a@example.com", tooLong, tooLong))
        assertTrue(violations.containsAll(setOf("password", "passwordConfirmation")))
        assertTrue(fields(CreateFirstUserRequest("Admin", "a@example.com", max, max)).isEmpty())
    }

    @Test
    fun `user creation rejects a password longer than 72 characters`() {
        val request =
            StoreUserRequest().apply {
                name = "User"
                email = "u@example.com"
                password = tooLong
            }
        assertTrue("password" in fields(request))
        request.password = max
        assertFalse("password" in fields(request))
    }

    @Test
    fun `password change rejects passwords longer than 72 characters`() {
        val violations = fields(UpdatePasswordRequest(tooLong, tooLong, tooLong))
        assertTrue(violations.containsAll(setOf("currentPassword", "password", "passwordConfirmation")))
        assertTrue(fields(UpdatePasswordRequest("current-password", max, max)).isEmpty())
    }
}
