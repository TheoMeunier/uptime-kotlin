package tmenier.fr.twofactor.dtos.requests

import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@RegisterForReflection
data class DisableTwoFactorRequest(
    @field:NotBlank(message = "Current password is required")
    @field:Size(max = 72, message = "Current password must be at most 72 characters long")
    val password: String,
    @field:NotBlank(message = "Code is required")
    @field:Size(max = 32, message = "Code is too long")
    val code: String,
)
