package tmenier.fr.setup.dto

import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@RegisterForReflection
data class CreateFirstUserRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 255, message = "Name must be at most 255 characters")
    val name: String,
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
    @field:Size(max = 255, message = "Email must be at most 255 characters")
    val email: String,
    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters long")
    val password: String,
    @field:NotBlank(message = "Confirmation password is required")
    @field:Size(min = 8, max = 72, message = "Confirmation password must be between 8 and 72 characters long")
    val passwordConfirmation: String,
)
