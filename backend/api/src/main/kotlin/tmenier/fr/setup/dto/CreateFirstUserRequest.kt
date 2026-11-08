package tmenier.fr.setup.dto

import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@RegisterForReflection
data class CreateFirstUserRequest(
    val name: String,
    val email: String,
    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters long")
    val password: String,
    @field:NotBlank(message = "Confirmation password is required")
    @field:Size(min = 8, max = 72, message = "Confirmation password must be between 8 and 72 characters long")
    val passwordConfirmation: String,
)
