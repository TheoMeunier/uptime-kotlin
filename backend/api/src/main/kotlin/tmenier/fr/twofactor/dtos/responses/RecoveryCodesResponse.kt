package tmenier.fr.twofactor.dtos.responses

import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
data class RecoveryCodesResponse(
    val recoveryCodes: List<String>,
)
