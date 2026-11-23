package tmenier.fr.twofactor.dtos.requests

import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@RegisterForReflection
data class RegenerateRecoveryCodesRequest(
    @field:NotBlank(message = "Code is required")
    @field:Size(max = 32, message = "Code is too long")
    val code: String,
)
