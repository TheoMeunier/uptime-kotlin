package tmenier.fr.auth.dtos.responses

import com.fasterxml.jackson.annotation.JsonInclude
import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_NULL)
data class LoginResponse(
    val token: String? = null,
    val refreshToken: String? = null,
    val sessionId: String? = null,
    val mfaRequired: Boolean = false,
    val challengeToken: String? = null,
)
