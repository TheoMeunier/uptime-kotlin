package tmenier.fr.auth.dtos.requests

import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@RegisterForReflection
data class LoginRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid format for email")
    val email: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters long")
    val password: String,
)
