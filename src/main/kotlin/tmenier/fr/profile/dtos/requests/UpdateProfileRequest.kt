package tmenier.fr.profile.dtos.requests

import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@RegisterForReflection
data class UpdateProfileRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid format for email")
    val email: String,
    @field:NotBlank(message = "Name is required")
    val name: String,
)

@RegisterForReflection
data class UpdatePasswordRequest(
    @field:NotBlank(message = "New password is required")
    @field:Size(min = 8, message = "New password must be at least 8 characters long")
    val password: String,
    @field:NotBlank(message = "Confirmation password is required")
    @field:Size(min = 8, message = "Confirmation password must be at least 8 characters long")
    val confirmPassword: String,
)
