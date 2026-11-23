package tmenier.fr.twofactor.dtos.responses

import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
data class TwoFactorSetupResponse(
    val secret: String,
    val otpauthUri: String,
)
