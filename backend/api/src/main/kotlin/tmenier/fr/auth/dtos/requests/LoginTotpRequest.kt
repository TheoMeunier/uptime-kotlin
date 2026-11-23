package tmenier.fr.auth.dtos.requests

import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@RegisterForReflection
data class LoginTotpRequest(
    @field:NotBlank(message = "Challenge token is required")
    @field:Size(max = 128, message = "Challenge token is too long")
    val challengeToken: String,
    @field:NotBlank(message = "Code is required")
    @field:Size(max = 32, message = "Code is too long")
    val code: String,
)
