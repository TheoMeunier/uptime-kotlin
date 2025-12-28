package tmenier.fr.auth.dtos.requests

import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@RegisterForReflection
data class RefreshTokenRequest(
    @field:NotBlank(message = "Refresh token must not be blank")
    val refreshToken: String,
)
