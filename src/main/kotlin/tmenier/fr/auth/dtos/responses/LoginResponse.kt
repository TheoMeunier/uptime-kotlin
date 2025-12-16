package tmenier.fr.auth.dtos.responses

import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
data class LoginResponse(
    val token: String,
    val refreshToken: String
)